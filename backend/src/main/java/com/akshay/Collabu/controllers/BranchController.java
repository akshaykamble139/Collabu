package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.services.BranchService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/branches")
public class BranchController {
    @Autowired
    private BranchService branchService;

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<BranchDTO>> getBranchesByRepositoryId(@PathVariable Long repositoryId, @AuthenticationPrincipal UserDetails userDetails) {
        List<BranchDTO> branches = branchService.getBranchesByRepoId(repositoryId, userDetails);
        return ResponseEntity.ok(branches);
    }
    
    @GetMapping("/{username}/{repoName}")
    public ResponseEntity<List<BranchDTO>> getBranchesByUserAndRepositoryName(
            @PathVariable String username,
            @PathVariable String repoName, @AuthenticationPrincipal UserDetails userDetails) {
        List<BranchDTO> branches = branchService.findByUsernameAndRepositoryName(username, repoName, userDetails);
        return ResponseEntity.ok(branches);
    }

    @PostMapping
    public ResponseEntity<BranchDTO> createBranch(@RequestBody @Valid BranchDTO branchDTO, @AuthenticationPrincipal UserDetails userDetails) {
        BranchDTO createdBranch = branchService.createBranch(branchDTO,userDetails);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBranch);
    }
    
    
    
    
    
}