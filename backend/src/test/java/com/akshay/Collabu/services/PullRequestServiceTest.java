package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

class PullRequestServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(PullRequestServiceTest.class);

    @Mock
    private PullRequestRepository pullRequestRepository;
    
    @Mock
    private BranchRepository branchRepository;
    
    @Mock
    private RepositoryRepository repositoryRepository;
    
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PullRequestService pullRequestService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetPullRequestsByRepoId() {
        List<PullRequest> pullRequests = new ArrayList<>();
        
        PullRequest pullRequest = new PullRequest();
        pullRequest.setId(1L);
        pullRequest.setTitle("Fix Bug");
        pullRequest.setStatus(PullRequestStatus.OPENED);
        
        Branch branch = new Branch();
        branch.setId(5L);
        branch.setName("master");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        branch.setRepository(repo);
        
        pullRequest.setRepository(repo);
        pullRequest.setSourceBranch(branch);
        pullRequest.setTargetBranch(branch);
        pullRequest.setCreatedBy(user);
        
        pullRequest.setIsDeleted(false);
        
        pullRequests.add(pullRequest);
        when(pullRequestRepository.findByRepositoryId(anyLong())).thenReturn(pullRequests);

        List<PullRequestDTO> result = pullRequestService.getPullRequestsByRepoId(1L);
        assertEquals(1, result.size());
        assertEquals("Fix Bug", result.get(0).getTitle()); 
        
    }
        
    @Test
    void testCreatePullRequest() {        
        PullRequest pullRequest = new PullRequest();
        pullRequest.setId(1L);
        pullRequest.setTitle("Fix Bug");
        pullRequest.setStatus(PullRequestStatus.OPENED);
        
        Branch branch = new Branch();
        branch.setId(5L);
        branch.setName("master");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        branch.setRepository(repo);
        
        pullRequest.setRepository(repo);
        pullRequest.setSourceBranch(branch);
        pullRequest.setTargetBranch(branch);
        pullRequest.setCreatedBy(user);
        
        pullRequest.setIsDeleted(false);
        
        PullRequestDTO pullRequestDTO = new PullRequestDTO(
        		pullRequest.getId(), 
        		pullRequest.getTitle(), 
        		pullRequest.getDescription(), 
        		pullRequest.getStatus().toString(), 
        		pullRequest.getRepository().getId(),
        		pullRequest.getSourceBranch().getId(),
        		pullRequest.getTargetBranch().getId(),
        		pullRequest.getCreatedBy().getId(),
        		pullRequest.getCreatedAt());
                
        try {
            when(repositoryRepository.findById(anyLong())).thenReturn(Optional.empty());
            pullRequestService.createPullRequest(pullRequestDTO);
	    }
	    catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        try {
            when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());
            pullRequestService.createPullRequest(pullRequestDTO);
	    }
	    catch (Exception e) {
			logger.info("Branch doesn't exist for id: {}", 1L);
		}
        pullRequestDTO.setDescription("WE fixed a difficult bug");
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));

        try {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
            pullRequestService.createPullRequest(pullRequestDTO);
	    }
	    catch (Exception e) {
			logger.info("User doesn't exist for id: {}", 1L);
		}
        

        when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
        when(pullRequestRepository.save(any())).thenReturn(pullRequest);

        PullRequestDTO result = pullRequestService.createPullRequest(pullRequestDTO);
        assertEquals("Fix Bug", result.getTitle()); 
        
    }
        
    @Test
    void testMergePullRequest() {        
        PullRequest pullRequest = new PullRequest();
        pullRequest.setId(1L);
        pullRequest.setTitle("Fix Bug");
        pullRequest.setStatus(PullRequestStatus.OPENED);
        
        Branch branch = new Branch();
        branch.setId(5L);
        branch.setName("master");
        
        User user = new User();
        user.setId(1L);
        user.setEmail("don@gmail.com");
        user.setUsername("don123");
        user.setRole("ROLE_USER");
        user.setIsActive(true);
        
        Repository_ repo = new Repository_();
        repo.setId(1L);
        repo.setName("TestRepo");
        repo.setDescription("Description");
        repo.setOwner(user);
        repo.setDefaultBranch("master");
        repo.setIsDeleted(false);
        repo.setStarsCount(0L);
        repo.setForksCount(0L);
        branch.setRepository(repo);
        
        pullRequest.setRepository(repo);
        pullRequest.setSourceBranch(branch);
        pullRequest.setTargetBranch(branch);
        pullRequest.setCreatedBy(user);
        
        pullRequest.setIsDeleted(false);
        
        try {
            when(pullRequestRepository.findById(anyLong())).thenReturn(Optional.empty());
            pullRequestService.mergePullRequest(1L);
	    }
	    catch (Exception e) {
			logger.info("Pull request doesn't exist for id: {}", 1L);
		}
        
        when(pullRequestRepository.findById(anyLong())).thenReturn(Optional.of(pullRequest));

        pullRequestService.mergePullRequest(1L);
        
    }


    
}
