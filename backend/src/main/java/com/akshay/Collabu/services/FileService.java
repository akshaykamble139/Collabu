package com.akshay.Collabu.services;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.FileContent;
import com.akshay.Collabu.models.FileContentLocation;
import com.akshay.Collabu.models.FileVersion;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.CommitRepository;
import com.akshay.Collabu.repositories.FileContentsRepository;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.FileVersionRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.UserRepository;

import jakarta.transaction.Transactional;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private FileVersionRepository fileVersionRepository;
    
    @Autowired
    private FileContentsRepository fileContentsRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private CommitRepository commitRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private Environment environment;
    
    private final Long SIZE_CUTOFF = 5 * 1024 * 1024L; // 5MB
    
	public static final Logger logger = LoggerFactory.getLogger(FileService.class);

    public List<FileDTO> getFilesByRepositoryId(Long repositoryId) {
        List<File> files = fileRepository.findByRepositoryId(repositoryId);
        return files.stream()
                .map(file -> mapEntityToDTO(file))
                .collect(Collectors.toList());
    }
    
    public FileDTO mapEntityToDTO(File file) {
		FileDTO resultDto = new FileDTO();
		resultDto.setId(file.getId());
        resultDto.setName(file.getName());
        resultDto.setRepositoryName(file.getRepository().getName());
        resultDto.setPath(file.getPath());
        resultDto.setType(file.getType());
        resultDto.setMimeType(file.getMimeType());
        
        return resultDto;
	}
    
    public FileDTO getFileById(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"File not found"));
        return mapEntityToDTO(file);
    }

    @Transactional
    public void createFile(FileDTO fileDTO, MultipartFile fileContents, UserDetails userDetails) {
        if (fileContents == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File not found");
        }

        User user = userRepository.getReferenceById(cacheService.getUserId(userDetails.getUsername()));
        Long repositoryId = cacheService.getRepositoryId(userDetails.getUsername() + "-" + fileDTO.getRepositoryName());

        if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found");
        }

        Repository_ repository = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));

        Branch branch = repository.getBranches().stream()
                .filter(brnch -> brnch.getName().equals(fileDTO.getBranchName()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

        boolean fileExists = fileRepository.existsByNameAndPathAndBranchId(
                fileDTO.getName(), fileDTO.getPath(), branch.getId()
        );
        if (fileExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "File already exists in this path.");
        }

        try {
            String hash = computeSHA1Hash(fileContents);

            Optional<FileContent> existingContent = fileContentsRepository.findByHash(hash);
            FileContent fileContent;

            if (existingContent.isEmpty()) {
                FileContentLocation location = determineStorageLocation(fileContents);
                fileContent = new FileContent();

                fileContent.setHash(hash);
                fileContent.setLocation(location);

                if (location == FileContentLocation.S3) {
                    handleS3Upload(fileContents, hash, fileContent);
                } else {
                    byte[] contentBytes = fileContents.getBytes();
                    fileContent.setContent(contentBytes);
                }

                fileContent = fileContentsRepository.save(fileContent);
            } else {
                fileContent = existingContent.get();
            }

            File file = new File();
            file.setName(fileDTO.getName());
            file.setPath(fileDTO.getPath());
            file.setRepository(repository);
            file.setBranch(branch);
            file.setLastModifiedAt(LocalDateTime.now());

            setFileMetadata(file, fileContents, fileContent);
            File savedFile = fileRepository.save(file);

            Commit savedCommit = saveCommit(fileDTO, user, branch, repository, savedFile);
            saveFileVersion(savedFile, hash, savedCommit, fileContents.getSize());

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process file");
        } 
    }

    // Determines if the file should go to S3 or DB
    private FileContentLocation determineStorageLocation(MultipartFile file) throws IOException {
        String mimeType = file.getContentType();
        boolean isBinary = mimeType != null && (
                mimeType.startsWith("image/") ||
                mimeType.startsWith("application/") ||
                mimeType.startsWith("video/") ||
                mimeType.startsWith("audio/")
        );
        return (file.getSize() > SIZE_CUTOFF || isBinary) ? FileContentLocation.S3 : FileContentLocation.DB;
    }

    // Handles S3 upload logic
    private void handleS3Upload(MultipartFile file, String hash, FileContent fileContent) throws IOException {
        String bucketName = environment.getProperty("aws.s3.bucket.name", "default-bucket");

        java.io.File tempFile = java.io.File.createTempFile("temp", file.getOriginalFilename());
        file.transferTo(tempFile);

        String s3Key = "files/" + hash;
        s3Service.uploadFile(bucketName, s3Key, tempFile.toPath());

        fileContent.setStorageUrl(s3Key);
    }

    // Extracts and sets metadata for the file
    private void setFileMetadata(File file, MultipartFile fileContents, FileContent fileContent) {
        String filename = fileContents.getOriginalFilename();
        if (filename != null && filename.contains(".")) {
            String extension = filename.substring(filename.lastIndexOf(".") + 1);
            file.setType(extension);
        }
        file.setMimeType(fileContents.getContentType());
        if (fileContent.getStorageUrl() != null) {
            file.setStorageUrl(fileContent.getStorageUrl());
        }
    }

    // Saves commit information
    private Commit saveCommit(FileDTO fileDTO, User user, Branch branch, Repository_ repository, File savedFile) {
        Commit commit = new Commit();
        commit.setBranch(branch);
        commit.setRepository(repository);
        commit.setTimestamp(savedFile.getLastModifiedAt());
        commit.setMessage(fileDTO.getCommitMessage() == null ? "File upload" : fileDTO.getCommitMessage());
        commit.setUser(user);
        return commitRepository.save(commit);
    }

    // Saves file version after the commit
    private void saveFileVersion(File file, String hash, Commit commit, long size) {
        FileVersion fileVersion = new FileVersion();
        fileVersion.setFile(file);
        fileVersion.setHash(hash);
        fileVersion.setCreatedAt(file.getLastModifiedAt());
        fileVersion.setVersionNumber(1);
        fileVersion.setSize(size);
        fileVersion.setCommit(commit);
        fileVersionRepository.save(fileVersion);
    }

    
    public void deleteFile(Long id) {
        if (!fileRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"File not found");
        }
        fileRepository.deleteById(id);
    }

    public FileDTO updateFile(FileDTO fileDTO) {
    	Long id = fileDTO.getId();
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"File not found"));
        file.setName(fileDTO.getName());
        File updatedFile = fileRepository.save(file);
        return mapEntityToDTO(updatedFile);
    }

	public List<FileDTO> getFilesByOwnerNameRepoNameAndBranchName(String username, String repoName, String branchName,
			UserDetails userDetails) {
		Long userId = cacheService.getUserId(username);
		if (userId == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User id not found for given username");
		}
		
		Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);
		if (repositoryId == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository id not found for given repository name");
		}
		
		Repository_ repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
		
		if (!username.equals(userDetails.getUsername()) && !repo.getVisibility().equals("public")) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
		}
		Branch branch = repo.getBranches().stream()
						.filter(brnch -> brnch.getName().equals(branchName))
						.findFirst()
						.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Branch not found"));
		
		return fileRepository.findByBranchId(branch.getId()).stream().map(file -> mapEntityToDTO(file)).collect(Collectors.toList());
	}
	
	public String computeSHA1Hash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }
	
	public List<FileDTO> getFilesByPath(String username, String repoName, String branchName, String path, UserDetails userDetails) {
        Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);
        Repository_ repo = repositoryRepository.findById(repositoryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));

        Branch branch = repo.getBranches().stream()
                .filter(brn -> brn.getName().equals(branchName))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

        List<File> files = fileRepository.findByBranchId(branch.getId()).stream()
                .filter(file -> file.getPath() != null && file.getPath().startsWith(path))
                .collect(Collectors.toList());

        return files.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
    }
		
	public ResponseEntity<?> getFileByPath(String username, String repoName, String branchName, String path, UserDetails userDetails) {
	    Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);
	    Repository_ repo = repositoryRepository.findById(repositoryId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found"));

	    Branch branch = repo.getBranches().stream()
	            .filter(brn -> brn.getName().equals(branchName))
	            .findFirst()
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));

	    String fileName = path.substring(path.lastIndexOf('/') + 1);
	    path = path.endsWith(fileName) ? path.substring(0, path.length() - fileName.length()) : path;

	    if (path.isEmpty()) {
	        path = "/";
	    } else if (path.charAt(0) != '/') {
	        path = "/" + path;
	    }
	    
	    final String filePath = path;

	    // Find file by path and name
	    File file = fileRepository.findByBranchId(branch.getId()).stream()
	            .filter(f -> f.getPath() != null && f.getPath().equals(filePath) && f.getName().equals(fileName))
	            .findFirst()
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "File not found"));

	    if (file.getSize() > SIZE_CUTOFF || isBinaryFile(file.getMimeType())) {
	        return handleLargeOrBinaryFile(file);
	    } else {
	        return handleSmallTextFile(file);
	    }
	}

	private ResponseEntity<?> handleLargeOrBinaryFile(File file) {
	    String bucketName = environment.getProperty("aws.s3.bucket.name", "default-bucket");
	    byte[] s3Content = s3Service.downloadFile(bucketName, file.getStorageUrl());

	    if (file.getMimeType().startsWith("image/")) {
	        // Return as Base64 for images
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_TYPE, file.getMimeType())
	                .body(Base64.getEncoder().encodeToString(s3Content));
	    } else {
	        // Offer as a direct download for other binary files
	        return ResponseEntity.ok()
	                .header(HttpHeaders.CONTENT_TYPE, file.getMimeType())
	                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
	                .contentLength(s3Content.length)
	                .body(s3Content);
	    }
	}

	private ResponseEntity<?> handleSmallTextFile(File file) {
	    FileDTO fileDTO = mapEntityToDTO(file);

	    // Fetch file contents for text files
	    List<FileVersion> currentFileVersions = fileVersionRepository.findByFileIdOrderByVersionNumberDesc(file.getId());
	    if (currentFileVersions == null || currentFileVersions.isEmpty()) {
	        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "File version not found");
	    }
	    String latestFileVersionHash = currentFileVersions.get(0).getHash();

	    fileContentsRepository.findByHash(latestFileVersionHash)
	            .ifPresent(content -> fileDTO.setContent(new String(content.getContent())));

	    return ResponseEntity.ok().body(fileDTO);
	}

    private boolean isBinaryFile(String mimeType) {
        return mimeType != null && (
                mimeType.startsWith("image/") ||
                mimeType.startsWith("application/") ||
                mimeType.startsWith("video/") ||
                mimeType.startsWith("audio/") ||
                mimeType.startsWith("font/"));
    }
}
