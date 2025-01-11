package com.akshay.Collabu.services;

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
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.FileVersion;
import com.akshay.Collabu.repositories.FileContentsRepository;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.FileVersionRepository;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private S3Service s3Service;
    
    @Autowired
    private FileVersionRepository fileVersionRepository;
    
    @Autowired
    private FileContentsRepository fileContentsRepository;
    
    @Autowired
    private CacheService cacheService;
    
    @Autowired
    private FileCacheService fileCacheService;
    
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
    	
    	return fileCacheService.mapEntityToDTO(file);
	}
    
    public FileDTO getFileById(Long id) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,"File not found"));
        return mapEntityToDTO(file);
    }

    public void createFile(FileDTO fileDTO, MultipartFile fileContents, UserDetails userDetails) {
        if (fileContents == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File not found");
        }

        Long repositoryId = cacheService.getRepositoryId(userDetails.getUsername() + "-" + fileDTO.getRepositoryName());

        if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found");
        }
        
        Long branchId = cacheService.getBranchId(userDetails.getUsername() + "-" + fileDTO.getRepositoryName() + "-" + fileDTO.getBranchName());

        if (branchId == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found");
        }
        
        String fullPath = fileDTO.getPath();
        if (!fullPath.startsWith("/")) {
            fullPath = "/" + fullPath;
        }
        
        if (!fullPath.endsWith("/")) {
            fullPath += "/";
        }
        
        fileCacheService.createFileForBranchIdAndPath(fileDTO, fileContents, branchId, fullPath);
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
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"User not found");
		}
		
		Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);
		if (repositoryId == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
		}
		
		Boolean isPublic = cacheService.getRepositoryVisibility(repositoryId);
    	
    	if (!username.equals(userDetails.getUsername()) && !isPublic) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
    	
    	Long branchId = cacheService.getBranchId(username + "-" + repoName + "-" + branchName);
		if (branchId == null) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Branch not found");
		}
		
		return fileCacheService.getFilesByBranchIdAndFilePath(branchId,"/");
	}
	
	public List<FileDTO> getFilesByPath(String username, String repoName, String branchName, String path, UserDetails userDetails) {

        Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);

        if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found");
        }
        
		Boolean isPublic = cacheService.getRepositoryVisibility(repositoryId);
    	
    	if (!username.equals(userDetails.getUsername()) && !isPublic) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
        
        Long branchId = cacheService.getBranchId(username + "-" + repoName + "-" + branchName);

        if (branchId == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found");
        }
        
        if (path.isEmpty()) {
            path = "/";
        } else if (!path.endsWith("/")) {
            path = path + "/";
        }
        
        if (path.charAt(0) != '/') {
	        path = "/" + path;
	    }
        
        final String filePath = path;
        
        return fileCacheService.getFilesByBranchIdAndFilePath(branchId, filePath);
    }
		
	public ResponseEntity<?> getFileByPath(String username, String repoName, String branchName, String path, UserDetails userDetails) {

	    Long repositoryId = cacheService.getRepositoryId(username + "-" + repoName);

        if (repositoryId == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Repository not found");
        }
        
		Boolean isPublic = cacheService.getRepositoryVisibility(repositoryId);
    	
    	if (!username.equals(userDetails.getUsername()) && !isPublic) {
    		throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Repository not found");
    	}
        
        Long branchId = cacheService.getBranchId(username + "-" + repoName + "-" + branchName);

        if (branchId == null) {
        	throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Branch not found");
        }
	    String fileName = path.substring(path.lastIndexOf('/') + 1);
	    path = path.endsWith(fileName) ? path.substring(0, path.length() - fileName.length()) : path;

	    if (path.isEmpty()) {
	        path = "/";
	    } else if (path.charAt(0) != '/') {
	        path = "/" + path;
	    }
	    
	    final String filePath = path;

	    // Find file by path and name
	    Optional<File> fileOptional = fileRepository.findByBranchId(branchId).stream()
	            .filter(f -> f.getPath() != null && f.getPath().equals(filePath) && f.getName().equals(fileName))
	            .findFirst();
	    
	    if (fileOptional.isEmpty()) {
	    	return ResponseEntity.ok(getFilesByPath(username, repoName, branchName, filePath + fileName + "/", userDetails));
	    }
	    File file = fileOptional.get();
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
