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

import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.services.RepositoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {
    @Autowired
    private RepositoryService repositoryService;
    
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<RepositoryDTO>> getRepositoriesByUserName(@PathVariable String userName) {
        List<RepositoryDTO> repositories = repositoryService.getRepositoriesByUserName(userName);
        return ResponseEntity.ok(repositories);
    }

    @PostMapping
    public ResponseEntity<RepositoryDTO> createRepository(@RequestBody @Valid RepositoryDTO repositoryDTO) {
        RepositoryDTO createdRepo = repositoryService.createRepository(repositoryDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRepo);
    }
    
    @GetMapping("/{username}/{repoName}")
    public ResponseEntity<RepositoryDTO> getRepositoryByUserAndName(
            @PathVariable String username,
            @PathVariable String repoName) {
        RepositoryDTO repo = repositoryService.findByUsernameAndName(username, repoName);
        return ResponseEntity.ok(repo);
    }

}

