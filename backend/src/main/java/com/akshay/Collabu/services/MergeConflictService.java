package com.akshay.Collabu.services;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.MergeConflictDTO;
import com.akshay.Collabu.models.MergeConflict;
import com.akshay.Collabu.repositories.MergeConflictRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class MergeConflictService {
	
	@Autowired
    private MergeConflictRepository mergeConflictRepository;

	private MergeConflictDTO mapToDTO(MergeConflict mergeConflict) {
        MergeConflictDTO dto = new MergeConflictDTO();
        dto.setId(mergeConflict.getId());
        dto.setPullRequestId(mergeConflict.getPullRequest().getId());
        dto.setConflictingFile(mergeConflict.getFile().getName());
        dto.setConflictDetails(mergeConflict.getConflictDetails());
        dto.setResolved(mergeConflict.isResolved());
        return dto;
    }
	
    public MergeConflict saveMergeConflict(MergeConflict mergeConflict) {
        return mergeConflictRepository.save(mergeConflict);
    }
    
    public List<MergeConflictDTO> getMergeConflictsByPullRequestId(Long pullRequestId) {
        List<MergeConflict> conflicts = mergeConflictRepository.findByPullRequestId(pullRequestId);
        return conflicts.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    public MergeConflictDTO resolveConflict(Long conflictId) {
        Optional<MergeConflict> conflictOptional = mergeConflictRepository.findById(conflictId);
        if (conflictOptional.isPresent()) {
            MergeConflict conflict = conflictOptional.get();
            conflict.setResolved(true);
            MergeConflict mergeConflict = mergeConflictRepository.save(conflict);
            
            return mapToDTO(mergeConflict);
        }
        throw new EntityNotFoundException("Merge conflict not found with ID: " + conflictId);
    }
}

