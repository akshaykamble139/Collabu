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

import com.akshay.Collabu.dto.FileVersionDTO;
import com.akshay.Collabu.services.FileVersionService;

@RestController
@RequestMapping("/api/file-versions")
public class FileVersionController {
	
	@Autowired
    private FileVersionService fileVersionService;
    
    @PostMapping
    public ResponseEntity<FileVersionDTO> createFileVersion(@RequestBody FileVersionDTO fileVersionDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(fileVersionService.saveFileVersion(fileVersionDTO));
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<List<FileVersionDTO>> getFileVersions(@PathVariable Long fileId) {
        return ResponseEntity.ok(fileVersionService.getFileVersionsByFileId(fileId));
    }

    @GetMapping("/{fileId}/version/{versionNumber}")
    public ResponseEntity<FileVersionDTO> getFileVersion(@PathVariable Long fileId, @PathVariable int versionNumber) {
        return ResponseEntity.ok(fileVersionService.getFileVersion(fileId,versionNumber));
    }
}

