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
import org.springframework.http.HttpStatus;
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
        
        if (file.getSize() > SIZE_CUTOFF && file.getStorageUrl() != null) {
        	
        }       
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
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"File not found"); 
    	}
    	
    	User user = userRepository.getReferenceById(cacheService.getUserId(userDetails.getUsername()));
    	
    	Long repositoryId = cacheService.getRepositoryId(userDetails.getUsername() + "-" + fileDTO.getRepositoryName());
    	if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
        // Validate repository existence
        Repository_ repository = repositoryRepository.findById(repositoryId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found"));
        
        Branch branch = repository.getBranches().stream()
				.filter(brnch -> brnch.getName().equals(fileDTO.getBranchName()))
				.findFirst()
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"Branch not found"));
        
        // Check for file name uniqueness within the same path in the repository
        boolean fileExists = fileRepository.existsByNameAndPathAndBranchId(
            fileDTO.getName(), fileDTO.getPath(), branch.getId()
        );
        if (fileExists) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,"File with this name already exists in the specified path in this branch.");
        }
        
        try {
			String hash = computeSHA1Hash(fileContents);
			
			// Check if hash already exists
	        Optional<FileContent> existingContent = fileContentsRepository.findByHash(hash);
	        FileContent fileContent = null;

	        if (existingContent.isEmpty()) {
	        	// Decide storage based on size
	            FileContentLocation location = fileContents.getSize() > SIZE_CUTOFF ? FileContentLocation.S3 : FileContentLocation.DB;
		        
	            fileContent = new FileContent();

	            fileContent.setHash(hash);
	            fileContent.setLocation(location);
	            
	            if (location.equals(FileContentLocation.S3)) {
	                String bucketName = environment.getProperty("aws.s3.bucket.name", "default-bucket");

	                java.io.File tempFile = java.io.File.createTempFile("temp", fileContents.getOriginalFilename());
	                fileContents.transferTo(tempFile);
	                	                
	                String s3Key = "files/" + hash;
	                s3Service.uploadFile(bucketName, s3Key, tempFile.toPath());
	                
	                fileContent.setStorageUrl(s3Key);
	                
	            } else {
	                byte[] contentBytes = fileContents.getBytes();
	                logger.debug("File size (bytes): {}", contentBytes.length);
	                
	                fileContent.setContent(contentBytes);
	            }
	            
	            fileContent = fileContentsRepository.save(fileContent);
	        }
	        else {
				fileContent = existingContent.get();
			}
	        
	        // Create and populate the File entity
	        File file = new File();
	        file.setName(fileDTO.getName());
	        file.setPath(fileDTO.getPath()); // Set the file's path
	        file.setRepository(repository);  // Associate the file with the repository
	        file.setBranch(branch);  // Associate the file with the branch
	        file.setLastModifiedAt(LocalDateTime.now()); // Optional: if metadata exists
	        
	        String filename = fileContents.getOriginalFilename(); // Gets the original filename of the uploaded file

	        if (filename != null && filename.contains(".")) {
	            String extension = filename.substring(filename.lastIndexOf(".") + 1);
	            file.setType(extension);
	        }	        
	        if (fileContent.getStorageUrl() != null) {
	        	file.setStorageUrl(fileContent.getStorageUrl());
	        }
	        // Save the file to the database
	        File savedFile = fileRepository.save(file);
	        
	        Commit commit = new Commit();
	        commit.setBranch(branch);
	        commit.setRepository(repository);
	        commit.setTimestamp(savedFile.getLastModifiedAt());
	        commit.setMessage(fileDTO.getCommitMessage() == null ? "" : fileDTO.getCommitMessage());
	        commit.setUser(user);
	        
	        Commit savedCommit = commitRepository.save(commit);
	        
	        FileVersion fileVersion = new FileVersion();
	        fileVersion.setFile(savedFile);
	        fileVersion.setHash(hash);
	        fileVersion.setCreatedAt(savedFile.getLastModifiedAt());
	        fileVersion.setVersionNumber(1);
	        fileVersion.setSize(fileContents.getSize());
	        fileVersion.setCommit(savedCommit);
	        
	        fileVersionRepository.save(fileVersion);	               
			
		} catch (Exception e) {
			e.printStackTrace();
			throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Excpetion occurred while reading file");
		}
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
}
