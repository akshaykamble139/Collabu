package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.services.FileService;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<FileDTO>> getFilesByRepositoryId(@PathVariable Long repositoryId) {
        List<FileDTO> files = fileService.getFilesByRepositoryId(repositoryId);
        return ResponseEntity.ok(files);
    }

    @PostMapping
    public ResponseEntity<FileDTO> createFile(@RequestBody FileDTO fileDTO) {
        FileDTO createdFile = fileService.createFile(fileDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdFile);
    }
}

