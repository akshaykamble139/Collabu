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

import com.akshay.Collabu.dto.CommitDTO;
import com.akshay.Collabu.services.CommitService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/commits")
public class CommitController {
    @Autowired
    private CommitService commitService;

    @GetMapping("/branch/{branchId}")
    public ResponseEntity<List<CommitDTO>> getCommitsByBranchId(@PathVariable Long branchId) {
        List<CommitDTO> commits = commitService.getCommitsByBranchId(branchId);
        return ResponseEntity.ok(commits);
    }
    
    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<CommitDTO>> getCommitsByRepositoryId(@PathVariable Long branchId) {
        List<CommitDTO> commits = commitService.getCommitsByRepositoryId(branchId);
        return ResponseEntity.ok(commits);
    }

    @PostMapping
    public ResponseEntity<CommitDTO> createCommit(@RequestBody @Valid CommitDTO commitDTO) {
        CommitDTO createdCommit = commitService.createCommit(commitDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdCommit);
    }
}
