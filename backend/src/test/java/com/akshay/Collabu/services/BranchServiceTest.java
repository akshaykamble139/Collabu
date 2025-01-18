package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
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
import org.springframework.security.core.userdetails.UserDetails;

import com.akshay.Collabu.dto.BranchDTO;
import com.akshay.Collabu.models.Branch;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.BranchRepository;
import com.akshay.Collabu.repositories.RepositoryRepository;

class BranchServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(BranchServiceTest.class);

    @Mock
    private RepositoryRepository repositoryRepository;
    
    @Mock
    private BranchRepository branchRepository;
    
    private UserDetails userDetails;

    @InjectMocks
    private BranchService branchService;
    
    @Mock
    private BranchCacheService branchCacheService;
    
    @Mock
    private CacheService cacheService;
    
    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        
        this.userDetails = org.springframework.security.core.userdetails.User.builder()
        .username("test")
        .password("testing")
        .roles("USER") 
        .build();
    }

    @Test
    void testGetBranchesByRepoId() {
        List<BranchDTO> branches = new ArrayList<>();
        BranchDTO branch = new BranchDTO();
        branch.setId(5L);
        branch.setName("master");
        branch.setRepositoryName("repo");
        branches.add(branch);

        when(cacheService.getRepositoryVisibility(anyLong())).thenReturn(true);
        
        when(branchCacheService.findBranchesByRepositoryId(anyLong())).thenReturn(branches);
        
        List<BranchDTO> result = branchService.getBranchesByRepoId(1L,userDetails);
        assertEquals(1, result.size());
        assertEquals("master", result.get(0).getName());
    }
        
    @Test
    void testcreateBranch() {
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
        
        
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setName(branch.getName());
        branchDTO.setId(branch.getId());
        branchDTO.setRepositoryName(branch.getRepository().getName());
        branchDTO.setParentBranchName("new_master");
        when(branchCacheService.mapEntityToDTO(any())).thenReturn(branchDTO);

        when(branchRepository.existsByRepositoryIdAndName(anyLong(), anyString())).thenReturn(true);
        
        try {
            branchService.createBranch(branchDTO,userDetails);
        }
        catch (Exception e) {
			logger.info("Branch already exists");
		}
        
        when(branchRepository.existsByRepositoryIdAndName(anyLong(), anyString())).thenReturn(false);
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));

        when(branchRepository.save(any())).thenReturn(branch);

        branchDTO.setName("master");
        when(branchCacheService.createBranchForRepositoryId(any(), anyLong())).thenReturn(branchDTO);
        BranchDTO result = branchService.createBranch(branchDTO,userDetails);
        assertEquals("master", result.getName());
    }
    
    @Test
    void testGetBranchById() {
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
        
        BranchDTO branchDTO = new BranchDTO();
        branchDTO.setName(branch.getName());
        branchDTO.setId(branch.getId());
        branchDTO.setRepositoryName(branch.getRepository().getName());
                
        when(branchRepository.findById(anyLong())).thenReturn(Optional.of(branch));
        when(branchCacheService.mapEntityToDTO(any())).thenReturn(branchDTO);
        BranchDTO result = branchService.getBranchById(1L);
        assertEquals("master", result.getName());
    }
    
    
	@Test
	void testDeleteBranchById() {
	                    
	    when(branchRepository.existsById(anyLong())).thenReturn(false);
	    
	    try {
	    	branchService.deleteBranchById(1L);
	    }
	    catch (Exception e) {
			logger.info("Branch doesn't exist for id: {}", 1L);
		}
	
	    when(branchRepository.existsById(anyLong())).thenReturn(true);
	
		branchService.deleteBranchById(1L);       
	}
}
