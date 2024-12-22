package com.akshay.Collabu.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.CommitDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.CommitRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

@Service
public class CommitService {
    @Autowired
    private CommitRepository commitRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;

    public List<CommitDTO> getCommitsByRepositoryId(Long repositoryId) {
        List<Commit> commits = commitRepository.findByRepositoryId(repositoryId);
        return commits.stream()
                .map(commit -> new CommitDTO(commit.getId(), commit.getMessage(), commit.getTimestamp(), commit.getRepository().getId(), commit.getBranch().getId()))
                .collect(Collectors.toList());
    }
    
	public List<CommitDTO> getCommitsByBranchId(Long branchId) {
        List<Commit> commits = commitRepository.findByBranchId(branchId);
        return commits.stream()
                .map(commit -> new CommitDTO(commit.getId(), commit.getMessage(), commit.getTimestamp(), commit.getRepository().getId(), commit.getBranch().getId()))
                .collect(Collectors.toList());
	}
    
    public CommitDTO getCommitById(Long id) {
        Commit commit = commitRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Commit not found"));
        return new CommitDTO(commit.getId(), commit.getMessage(), commit.getTimestamp(), commit.getRepository().getId(), commit.getBranch().getId());
    }

    public CommitDTO createCommit(CommitDTO commitDTO) {
        Commit commit = new Commit();
        
        Branch branch = branchRepository.findById(commitDTO.getBranchId())
        		.orElseThrow(() -> new RuntimeException("Branch not found"));
        
        commit.setBranch(branch);
        
        Repository_ repo = repositoryRepository.findById(commitDTO.getRepositoryId())
                .orElseThrow(() -> new RuntimeException("Repository not found"));
        
        commit.setRepository(repo);
        commit.setMessage(commitDTO.getMessage());
        Commit savedCommit = commitRepository.save(commit);
        return new CommitDTO(savedCommit.getId(), savedCommit.getMessage(), savedCommit.getTimestamp(), savedCommit.getRepository().getId(), savedCommit.getBranch().getId());
    }
}