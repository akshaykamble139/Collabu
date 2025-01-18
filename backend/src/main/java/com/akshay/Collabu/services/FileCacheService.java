package com.akshay.Collabu.services;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.dto.UpdateFileRequestDTO;
import com.akshay.Collabu.models.ActivityAction;
import com.akshay.Collabu.models.ActivityLog;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.FileContent;
import com.akshay.Collabu.models.FileContentLocation;
import com.akshay.Collabu.models.FileVersion;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.ActivityLogRepository;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.CommitRepository;
import com.akshay.Collabu.repositories.FileContentsRepository;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.FileVersionRepository;

import jakarta.transaction.Transactional;

@Service
public class FileCacheService {
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private FileVersionRepository fileVersionRepository;
    
    @Autowired
    private FileContentsRepository fileContentsRepository;
    
    @Autowired
    private CommitRepository commitRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private ActivityLogRepository activityLogRepository;
    
    @Autowired
    private Environment environment;
    
    private final Long SIZE_CUTOFF = 5 * 1024 * 1024L; // 5MB
    
	public static final Logger logger = LoggerFactory.getLogger(FileCacheService.class);
    
    public FileDTO mapEntityToDTO(File file) {
		FileDTO resultDto = new FileDTO();
		resultDto.setId(file.getId());
        resultDto.setName(file.getName());
        resultDto.setRepositoryName(file.getRepository().getName());
        resultDto.setPath(file.getPath());
        resultDto.setType(file.getType());
        resultDto.setMimeType(file.getMimeType());
        
        if (isEditableMimeType(file.getMimeType()) && file.getSize() <= SIZE_CUTOFF && file.getStorageUrl() == null) {
        	resultDto.setIsEditable(true);
        }
        
        return resultDto;
	}

    @Transactional
    @Caching(evict = {
	        @CacheEvict(value = "files", key = "#branchId + '-' + #fullPath")
	    })
    public void createFileForBranchIdAndPath(FileDTO fileDTO, MultipartFile fileContents, Long branchId, String fullPath) {
    	
        Branch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found"));
        
        boolean fileExists = fileRepository.existsByNameAndPathAndBranchId(
                fileDTO.getName(), fullPath, branch.getId()
        );
        if (fileExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "File already exists in this path.");
        }

        try {
            String hash = computeSHA1Hash(fileContents.getBytes());

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
            
            Repository_ repository = branch.getRepository();
            User user = repository.getOwner();

            File file = new File();
            file.setName(fileDTO.getName());
            file.setPath(fullPath);
            file.setRepository(repository);
            file.setBranch(branch);
            file.setLastModifiedAt(LocalDateTime.now());

            setFileMetadata(file, fileContents, fileContent);
            File savedFile = fileRepository.save(file);

            Commit savedCommit = saveCommit(fileDTO.getCommitMessage(), user, branch, repository, savedFile);
            saveFileVersion(savedFile, hash, savedCommit, fileContents.getSize(), 1);
            saveActivityLog(user, branch, repository, savedFile, ActivityAction.CREATE_FILE);

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
    private Commit saveCommit(String commitMessage, User user, Branch branch, Repository_ repository, File savedFile) {
        Commit commit = new Commit();
        commit.setBranch(branch);
        commit.setRepository(repository);
        commit.setTimestamp(savedFile.getLastModifiedAt());
        commit.setMessage(commitMessage == null ? "File upload" : commitMessage);
        commit.setUser(user);
        Commit savedCommit = commitRepository.save(commit);
        
        branch.setLastCommit(savedCommit);
        branchRepository.save(branch);
        
        return savedCommit;
    }

	// Log activity
    private void saveActivityLog( User user, Branch branch, Repository_ repository, File file, ActivityAction action) {
        ActivityLog log = new ActivityLog();
        log.setAction(action);
        log.setUserId(user.getId());
        log.setRepositoryId(repository.getId());
        log.setBranchId(branch.getId());
        log.setFileId(file.getId());
        log.setTimestamp(LocalDateTime.now());
        activityLogRepository.save(log);
	}

    // Saves file version after the commit
    private void saveFileVersion(File file, String hash, Commit commit, long size, Integer versionNumber) {
        FileVersion fileVersion = new FileVersion();
        fileVersion.setFile(file);
        fileVersion.setHash(hash);
        fileVersion.setCreatedAt(file.getLastModifiedAt());
        fileVersion.setVersionNumber(versionNumber);
        fileVersion.setSize(size);
        fileVersion.setCommit(commit);
        fileVersionRepository.save(fileVersion);
    }

    @Cacheable(value = "files", key = "#branchId + '-' + #filePath")
	public List<FileDTO> getFilesByBranchIdAndFilePath(Long branchId, String filePath) {
    	
		List<File> allFiles = fileRepository.findByBranchId(branchId).stream()
                .filter(file -> file.getPath() != null && file.getPath().startsWith(filePath))
                .collect(Collectors.toList());
        
        // Files directly in the given directory
        List<File> filesInDirectory = allFiles.stream()
                .filter(file -> file.getPath() != null && file.getPath().equals(filePath))
                .collect(Collectors.toList());

        // Folders (subdirectories directly under the given directory)
        Set<String> folderNames = allFiles.stream()
                .filter(file -> file.getPath() != null && file.getPath().startsWith(filePath) && !file.getPath().equals(filePath))
                .map(file -> {
                    // Extract the immediate next directory name from the path
                    String subPath = file.getPath().substring(filePath.length());
                    int slashIndex = subPath.indexOf('/');
                    return (slashIndex != -1) ? subPath.substring(0, slashIndex) : null;
                })
                .filter(Objects::nonNull) // Filter out null values
                .collect(Collectors.toSet());

        // Map files to DTOs
        List<FileDTO> fileDTOs = filesInDirectory.stream()
                .map(this::mapEntityToDTO)
                .collect(Collectors.toList());

        // Add folders as DTOs
        for (String folderName : folderNames) {
            FileDTO folderDTO = new FileDTO();
            folderDTO.setName(folderName);
            folderDTO.setPath(filePath);
            folderDTO.setType("folder"); // Mark it as a folder
            folderDTO.setMimeType(null);
            fileDTOs.add(folderDTO);
        }

        return fileDTOs;
	}
    
    @Transactional
    @Caching(evict = {
	        @CacheEvict(value = "files", key = "#branch.getId() + '-' + #newFilePath"),
	        @CacheEvict(value = "files", key = "#branch.getId() + '-' + #oldFilePath")
	    })
    public Boolean updateFile(File file, Repository_ repository, Branch branch, String newFilePath, String oldFilePath, UpdateFileRequestDTO requestDTO, FileVersion lastFileVersion, Boolean isFileContentsChanged) throws NoSuchAlgorithmException, IOException {
        User user = repository.getOwner();

        Integer latestVersion = lastFileVersion.getVersionNumber() + 1;
        String hash = lastFileVersion.getHash();
        
        if (isFileContentsChanged) {
        	byte[] fileContentBytes = requestDTO.getFileContent().getBytes(StandardCharsets.UTF_8);
			
			String newHash = computeSHA1Hash(fileContentBytes);
			
            Optional<FileContent> existingContent = fileContentsRepository.findByHash(newHash);
            FileContent fileContent;

            if (existingContent.isEmpty()) {
                fileContent = new FileContent();

                fileContent.setHash(newHash);
                fileContent.setLocation(FileContentLocation.DB);

                byte[] contentBytes = fileContentBytes;
                fileContent.setContent(contentBytes);
                fileContent = fileContentsRepository.save(fileContent);
                
                hash = newHash;
            } else {
                fileContent = existingContent.get();
                
                hash = fileContent.getHash();
            }
            
            file.setSize((long) fileContentBytes.length);
	            
        }
        
        Long size = file.getSize();
        
        file.setLastModifiedAt(LocalDateTime.now());

        File savedFile = fileRepository.save(file);
        
        if (requestDTO.getMessage() == null || requestDTO.getMessage().isEmpty()) {
        	requestDTO.setMessage("Update " + file.getName());
        }

        Commit savedCommit = saveCommit(requestDTO.getMessage(), user, branch, repository, savedFile);
        
        saveFileVersion(savedFile, hash, savedCommit, size, latestVersion);
        saveActivityLog(user, branch, repository, savedFile, ActivityAction.UPDATE_FILE);
		
		return true;
	}
	
	public String computeSHA1Hash(byte[] fileContents) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(fileContents);
        return Base64.getEncoder().encodeToString(hashBytes);
    }	
	
	public boolean isEditableMimeType(String mimeType) {
        // Define editable MIME types
        return mimeType != null && (
            mimeType.startsWith("text/") ||
            mimeType.equals("application/json") ||
            mimeType.equals("application/xml") ||
            mimeType.equals("application/javascript")
        );
    }
}
