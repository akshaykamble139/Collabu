package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.models.File;
import com.akshay.Collabu.repositories.FileRepository;

@Service
public class FileService {
    @Autowired
    private FileRepository fileRepository;

    public List<FileDTO> getFilesByRepositoryId(Long repositoryId) {
        List<File> files = fileRepository.findByRepository_Id(repositoryId);
        return files.stream()
                .map(file -> new FileDTO(file.getId(), file.getName(), file.getContent(), file.getRepository().getId()))
                .collect(Collectors.toList());
    }

    public FileDTO createFile(FileDTO fileDTO) {
        File file = new File();
        file.setName(fileDTO.getName());
        file.setContent(fileDTO.getContent());
        // Assume the repository is fetched and set
        File savedFile = fileRepository.save(file);
        return new FileDTO(savedFile.getId(), savedFile.getName(), savedFile.getContent(), savedFile.getRepository().getId());
    }
}

