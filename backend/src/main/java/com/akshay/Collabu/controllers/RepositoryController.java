package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.services.RepositoryService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/repositories")
public class RepositoryController {
    @Autowired
    private RepositoryService repositoryService;
    
    @GetMapping("/user/{userName}")
    public ResponseEntity<List<RepositoryDTO>> getRepositoriesByUserName(@PathVariable String userName, @AuthenticationPrincipal UserDetails userDetails) {
        List<RepositoryDTO> repositories = repositoryService.getRepositoriesByUserName(userName, userDetails);
        return ResponseEntity.ok(repositories);
    }

    @PostMapping
    public ResponseEntity<RepositoryDTO> createRepository(@RequestBody @Valid RepositoryDTO repositoryDTO,@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails.getUsername().equals(repositoryDTO.getOwnerUsername())) {
	    	RepositoryDTO createdRepo = repositoryService.createRepository(repositoryDTO);
	        return ResponseEntity.status(HttpStatus.CREATED).body(createdRepo);
        }
        else {
        	throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authorized to create repository for other users");
        }
    }
    
    @GetMapping("/{username}/{repoName}")
    public ResponseEntity<RepositoryDTO> getRepositoryByUserAndName(
            @PathVariable String username,
            @PathVariable String repoName, @AuthenticationPrincipal UserDetails userDetails) {
        RepositoryDTO repo = repositoryService.findByUsernameAndName(username, repoName, userDetails);
        return ResponseEntity.ok(repo);
    }
    
    @DeleteMapping("/{repoId}")
    public ResponseEntity<String> deleteRepository(@PathVariable Long repoId,@AuthenticationPrincipal UserDetails userDetails) {
	    	repositoryService.deleteRepository(repoId, userDetails);
	        return ResponseEntity.status(HttpStatusCode.valueOf(204)).body("Account deleted succesfully!");
    }

}

