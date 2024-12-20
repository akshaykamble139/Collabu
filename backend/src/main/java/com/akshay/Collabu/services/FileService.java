package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.FileRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;

    public List<FileDTO> getFilesByRepositoryId(Long repositoryId) {
        List<File> files = fileRepository.findByRepository_Id(repositoryId);
        return files.stream()
                .map(file -> new FileDTO(file.getId(), file.getName(), file.getContent(), file.getPath(), file.getType(), file.getRepository().getId()))
                .collect(Collectors.toList());
    }

    public FileDTO createFile(FileDTO fileDTO) {
        // Validate repository existence
        Repository_ repository = repositoryRepository.findById(fileDTO.getRepositoryId())
            .orElseThrow(() -> new RuntimeException("Repository not found"));

        // Check for file name uniqueness within the same path in the repository
        boolean fileExists = fileRepository.existsByNameAndPathAndRepositoryId(
            fileDTO.getName(), fileDTO.getPath(), fileDTO.getRepositoryId()
        );
        if (fileExists) {
            throw new RuntimeException("File with this name already exists in the specified path.");
        }

        // Create and populate the File entity
        File file = new File();
        file.setName(fileDTO.getName());
        file.setContent(fileDTO.getContent());
        file.setPath(fileDTO.getPath()); // Set the file's path
        file.setRepository(repository);  // Associate the file with the repository
        file.setLastModifiedAt(LocalDateTime.now()); // Optional: if metadata exists

        // Save the file to the database
        File savedFile = fileRepository.save(file);

        // Return the saved file as a DTO
        return new FileDTO(savedFile.getId(), savedFile.getName(), savedFile.getContent(), 
            savedFile.getPath(), savedFile.getType(), savedFile.getRepository().getId());
    }


    public void deleteFile(Long id) {
        if (!fileRepository.existsById(id)) {
            throw new RuntimeException("File not found");
        }
        fileRepository.deleteById(id);
    }

    public FileDTO updateFile(Long id, FileDTO fileDTO) {
        File file = fileRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));
        file.setName(fileDTO.getName());
        file.setContent(fileDTO.getContent());
        File updatedFile = fileRepository.save(file);
        return new FileDTO(updatedFile.getId(), updatedFile.getName(), updatedFile.getContent(), updatedFile.getPath(), updatedFile.getType(), updatedFile.getRepository().getId());
    }
}
