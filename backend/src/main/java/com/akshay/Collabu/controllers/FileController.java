package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.FileDTO;
import com.akshay.Collabu.services.FileService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

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
    
    @GetMapping("/{username}/{repoName}/{branchName}")
    public ResponseEntity<List<FileDTO>> getFilesByOwnerNameRepoNameAndBranchName(
    		@PathVariable String username,
    		@PathVariable String repoName,
    		@PathVariable String branchName,
    		@AuthenticationPrincipal UserDetails userDetails
    		) {
        List<FileDTO> files = fileService.getFilesByOwnerNameRepoNameAndBranchName(username,repoName,branchName,userDetails);
        return ResponseEntity.ok(files);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createFile(@RequestPart(value = "fileDTO") String fileDTOJson,@RequestPart("file") MultipartFile fileContents,  @AuthenticationPrincipal UserDetails userDetails) {
        System.out.println("Received fileDTO JSON: " + fileDTOJson);
        System.out.println("Received file name: " + fileContents.getOriginalFilename());

    	// Manually deserialize the JSON string into a FileDTO object    	
        ObjectMapper objectMapper = new ObjectMapper();
        FileDTO fileDTO;
        try {
            fileDTO = objectMapper.readValue(fileDTOJson, FileDTO.class);
        } catch (JsonProcessingException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid JSON format for fileDTO", e);
        }
    	fileService.createFile(fileDTO, fileContents, userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body("File created successfully!");
    }
}

