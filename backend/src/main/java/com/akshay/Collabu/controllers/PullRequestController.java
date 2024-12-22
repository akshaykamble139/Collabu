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

import com.akshay.Collabu.dto.PullRequestDTO;
import com.akshay.Collabu.services.PullRequestService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/pull-requests")
public class PullRequestController {
    @Autowired
    private PullRequestService pullRequestService;

    @GetMapping("/repository/{repositoryId}")
    public ResponseEntity<List<PullRequestDTO>> getPullRequestsByRepositoryId(@PathVariable Long repositoryId) {
        List<PullRequestDTO> pullRequests = pullRequestService.getPullRequestsByRepoId(repositoryId);
        return ResponseEntity.ok(pullRequests);
    }

    @PostMapping
    public ResponseEntity<PullRequestDTO> createPullRequest(@RequestBody @Valid PullRequestDTO pullRequestDTO) {
        PullRequestDTO createdPullRequest = pullRequestService.createPullRequest(pullRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdPullRequest);
    }

    @PostMapping("/{id}/merge")
    public ResponseEntity<String> mergePullRequest(@PathVariable Long id) {
        pullRequestService.mergePullRequest(id);
        return ResponseEntity.ok("Pull request merged successfully.");
    }
}