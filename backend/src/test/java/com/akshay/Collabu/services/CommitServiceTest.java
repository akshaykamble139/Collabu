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

import com.akshay.Collabu.dto.CommitDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Commit;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.CommitRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

class CommitServiceTest {
	public static final Logger logger = LoggerFactory.getLogger(CommitServiceTest.class);

    @Mock
    private CommitRepository commitRepository;
    
    @Mock
    private BranchRepository branchRepository;
    
    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private CommitService commitService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCommitById() {
        Commit commit = new Commit();
        
        commit.setId(1L);
        commit.setMessage("Initial commit");
        commit.setIsDeleted(false);
        
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
        
        commit.setUser(user);
        commit.setBranch(branch);
        commit.setRepository(repo);
        
        try {
            when(commitRepository.findById(anyLong())).thenReturn(Optional.empty());
	    	commitService.getCommitById(1L);
	    }
	    catch (Exception e) {
			logger.info("Commit doesn't exist for id: {}", 1L);
		}
        
        when(commitRepository.findById(anyLong())).thenReturn(Optional.of(commit));

        CommitDTO result = commitService.getCommitById(1L);
        assertEquals("Initial commit", result.getMessage());
    }
        
    @Test
    void testGetCommitsByRepositoryId() {
        Commit commit = new Commit();
        
        commit.setId(1L);
        commit.setMessage("Initial commit");
        commit.setIsDeleted(false);
        
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
        
        commit.setUser(user);
        commit.setBranch(branch);
        commit.setRepository(repo);
        
        List<Commit> commits = new ArrayList<Commit>();
        commits.add(commit);
        
        when(commitRepository.findByRepositoryId(anyLong())).thenReturn(commits);

        List<CommitDTO> result = commitService.getCommitsByRepositoryId(1L);
        assertEquals("Initial commit", result.get(0).getMessage());
    }
    
    @Test
    void testGetCommitsByBranchId() {
        Commit commit = new Commit();
        
        commit.setId(1L);
        commit.setMessage("Initial commit");
        commit.setIsDeleted(false);
        
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
        
        commit.setUser(user);
        commit.setBranch(branch);
        commit.setRepository(repo);
        
        List<Commit> commits = new ArrayList<Commit>();
        commits.add(commit);
        
        when(commitRepository.findByBranchId(anyLong())).thenReturn(commits);

        List<CommitDTO> result = commitService.getCommitsByBranchId(1L);
        assertEquals("Initial commit", result.get(0).getMessage());
    }
        
    @Test
    void testcreateCommit() {
        Commit commit = new Commit();
        
        commit.setId(1L);
        commit.setMessage("Initial commit");
        commit.setIsDeleted(false);
        
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
        
        commit.setUser(user);
        commit.setBranch(branch);
        commit.setRepository(repo);
        
        CommitDTO commitDTO = new CommitDTO(commit.getId(), commit.getMessage(), commit.getTimestamp(), commit.getRepository().getId(), commit.getBranch().getId());
        
        
        when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());
        
        try {
            when(branchRepository.findById(anyLong())).thenReturn(Optional.empty());
	    	commitService.createCommit(commitDTO);
	    }
	    catch (Exception e) {
			logger.info("Branch doesn't exist for id: {}", 1L);
		}
        
        try {
        	when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
            when(repositoryRepository.findById(anyLong())).thenReturn(Optional.empty());
            commitService.createCommit(commitDTO);
	    }
	    catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}
        
    	when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        when(commitRepository.save(any())).thenReturn(commit);
        
        CommitDTO result = commitService.createCommit(commitDTO);
        assertEquals("Initial commit", result.getMessage());
    }
    
    
    
}