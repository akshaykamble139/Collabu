package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

@Service
public class BranchService {
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;

    public List<BranchDTO> getBranchesByRepoId(Long repoId) {
        return branchRepository.findByRepositoryId(repoId).stream()
                .map(branch -> new BranchDTO(branch.getId(), branch.getName(), branch.getRepository().getId()))
                .collect(Collectors.toList());
    }

    public BranchDTO createBranch(BranchDTO branchDTO) {
        // Check if a branch with the same name already exists for this repository
        boolean exists = branchRepository.existsByRepositoryIdAndName(branchDTO.getRepositoryId(),branchDTO.getName());
        if (exists) {
            throw new RuntimeException("Branch with this name already exists for this repository.");
        }
        Branch branch = new Branch();
        branch.setName(branchDTO.getName());
        
        Repository_ repo = repositoryRepository.findById(branchDTO.getRepositoryId())
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        
        branch.setRepository(repo);
        branch.getRepository().setId(branchDTO.getRepositoryId());
        Branch savedBranch = branchRepository.save(branch);
        return new BranchDTO(savedBranch.getId(), savedBranch.getName(), savedBranch.getRepository().getId());
    }

    public BranchDTO getBranchById(Long id) {
        Branch branch = branchRepository.findById(id)
        		.orElseThrow(() -> new RuntimeException("Branch not found"));
        return new BranchDTO(branch.getId(), branch.getName(), branch.getRepository().getId());
    }
    
    public void deleteBranchById(Long id) {
    	if (!branchRepository.existsById(id)) {
            throw new RuntimeException("Branch not found");
        }
    	branchRepository.deleteById(id);
	}
}