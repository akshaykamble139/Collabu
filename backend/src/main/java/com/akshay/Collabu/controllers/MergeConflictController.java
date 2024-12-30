package com.akshay.Collabu.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.akshay.Collabu.dto.MergeConflictDTO;
import com.akshay.Collabu.services.MergeConflictService;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/api/merge-conflicts")
public class MergeConflictController {
	
	@Autowired
    private MergeConflictService mergeConflictService;

	@GetMapping("/{pullRequestId}")
    public ResponseEntity<List<MergeConflictDTO>> getConflictsByPullRequest(@PathVariable Long pullRequestId) {
        return ResponseEntity.ok(mergeConflictService.getMergeConflictsByPullRequestId(pullRequestId));
    }

    @PatchMapping("/{conflictId}/resolve")
    public ResponseEntity<MergeConflictDTO> resolveConflict(@PathVariable Long conflictId) {
        try {
            MergeConflictDTO resolvedConflict = mergeConflictService.resolveConflict(conflictId);
            return ResponseEntity.ok(resolvedConflict);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

