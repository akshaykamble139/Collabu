package com.akshay.Collabu.services;

import java.io.IOException;
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
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.FileContent;
import com.akshay.Collabu.models.FileContentLocation;
import com.akshay.Collabu.models.FileVersion;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
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
	
	public String computeSHA1Hash(MultipartFile file) throws IOException, NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] hashBytes = digest.digest(file.getBytes());
        return Base64.getEncoder().encodeToString(hashBytes);
    }	
}
