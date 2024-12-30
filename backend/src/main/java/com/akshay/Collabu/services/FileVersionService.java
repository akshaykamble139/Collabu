package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.FileVersionDTO;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.FileVersion;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.FileVersionRepository;

@Service
public class FileVersionService {
	
	@Autowired
    private FileRepository fileRepository;
	
	@Autowired
    private FileVersionRepository fileVersionRepository;

	private FileVersionDTO mapToDTO(FileVersion fileVersion) {
        FileVersionDTO dto = new FileVersionDTO();
        dto.setId(fileVersion.getId());
        dto.setFileId(fileVersion.getFile().getId());
        dto.setVersionNumber(fileVersion.getVersionNumber());
        dto.setContent(fileVersion.getContent());
        dto.setCreatedAt(fileVersion.getCreatedAt());
        return dto;
    } 
	public FileVersionDTO saveFileVersion(FileVersionDTO fileVersionDTO) {
        FileVersion fileVersion = new FileVersion();
        File file = fileRepository.findById(fileVersionDTO.getFileId())
                .orElseThrow(() -> new RuntimeException("File not found"));
        fileVersion.setFile(file);
        fileVersion.setVersionNumber(fileVersionDTO.getVersionNumber());
        fileVersion.setContent(fileVersionDTO.getContent());

        FileVersion savedFileVersion = fileVersionRepository.save(fileVersion);

        return mapToDTO(savedFileVersion);
    }
    
    public List<FileVersionDTO> getFileVersionsByFileId(Long fileId) {
        List<FileVersion> fileVersions = fileVersionRepository.findByFileId(fileId);
        return fileVersions.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public FileVersionDTO getFileVersion(Long fileId, int versionNumber) {
        FileVersion fileVersion = fileVersionRepository.findByFileIdAndVersionNumber(fileId, versionNumber)
        		.orElseThrow(() -> new RuntimeException("File version not found"));
        
        return mapToDTO(fileVersion);
    }
}
