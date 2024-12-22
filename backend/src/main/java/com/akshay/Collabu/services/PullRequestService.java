package com.akshay.Collabu.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.PullRequestDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.PullRequest;
import com.akshay.Collabu.models.PullRequestStatus;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.PullRequestRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.UserRepository;

@Service
public class PullRequestService {
    @Autowired
    private PullRequestRepository pullRequestRepository;
    
    @Autowired
    private BranchRepository branchRepository;
    
    @Autowired
    private RepositoryRepository repositoryRepository;
    
    @Autowired
    private UserRepository userRepository;

    public List<PullRequestDTO> getPullRequestsByRepoId(Long repoId) {
        return pullRequestRepository.findByRepositoryId(repoId).stream()
                .map(pr -> new PullRequestDTO(
                		pr.getId(), 
                		pr.getTitle(), 
                		pr.getDescription(), 
                		pr.getStatus().toString(), 
                		pr.getRepository().getId(),
                		pr.getSourceBranch().getId(),
                		pr.getTargetBranch().getId(),
                		pr.getCreatedBy().getId(),
                		pr.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public PullRequestDTO createPullRequest(PullRequestDTO pullRequestDTO) {
    	PullRequest pullRequest = new PullRequest();
    	
    	pullRequest.setTitle(pullRequestDTO.getTitle());
    	
    	if (pullRequestDTO.getDescription() != null && !pullRequestDTO.getDescription().isBlank()) {
    		pullRequest.setDescription(pullRequestDTO.getDescription());
    	}
    	
    	try {
        	pullRequest.setStatus(PullRequestStatus.valueOf(pullRequestDTO.getStatus().toUpperCase()));

		} catch (IllegalArgumentException ex) {
			throw new RuntimeException("Incorrect status provided");
		}
    	
    	Repository_ repo = repositoryRepository.findById(pullRequestDTO.getRepositoryId())
                .orElseThrow(() -> new RuntimeException("Repository not found"));
    	
    	pullRequest.setRepository(repo);
    	
    	
    	Branch sourceBranch = branchRepository.findById(pullRequestDTO.getSourceBranchId())
        		.orElseThrow(() -> new RuntimeException("Source branch not found"));
    	
    	pullRequest.setSourceBranch(sourceBranch);
    	
    	Branch targetBranch = branchRepository.findById(pullRequestDTO.getTargetBranchId())
        		.orElseThrow(() -> new RuntimeException("Target branch not found"));
    	
    	pullRequest.setTargetBranch(targetBranch);
    	
    	User user = userRepository.findById(pullRequestDTO.getCreatedByUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    	
    	pullRequest.setCreatedBy(user);
    	
    	pullRequest.setCreatedAt(LocalDateTime.now());
    	
        PullRequest savedPullRequest = pullRequestRepository.save(pullRequest);
        
        return new PullRequestDTO(
    		savedPullRequest.getId(), 
    		savedPullRequest.getTitle(), 
    		savedPullRequest.getDescription(), 
    		savedPullRequest.getStatus().toString(), 
    		savedPullRequest.getRepository().getId(),
    		savedPullRequest.getSourceBranch().getId(),
    		savedPullRequest.getTargetBranch().getId(),
    		savedPullRequest.getCreatedBy().getId(),
    		savedPullRequest.getCreatedAt());
    }

    public void mergePullRequest(Long prId) {
        PullRequest pr = pullRequestRepository.findById(prId)
                .orElseThrow(() -> new RuntimeException("Pull Request not found"));
        pr.setStatus(PullRequestStatus.MERGED);
        pullRequestRepository.save(pr);
    }
}