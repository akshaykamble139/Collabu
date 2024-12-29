package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.akshay.Collabu.dto.RepositoryDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.UserRepository;

class RepositoryServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(RepositoryServiceTest.class);

    @Mock
    private RepositoryRepository repositoryRepository;
	
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private CacheService cacheService;

    @InjectMocks
    private RepositoryService repositoryService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRepositoryByIdExistingRepo() {
        
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
        
        
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));

        RepositoryDTO result = repositoryService.getRepositoryById(1L);
        assertEquals("TestRepo", result.getName());
    }
    
    @Test
    void testGetRepositoryByIdNotExistingRepo() {       
        
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.empty());

        try {
        RepositoryDTO result = repositoryService.getRepositoryById(1L);
        }
        catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}
    }
	    
    @Test
    void testCreateRepository() {

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
        
        RepositoryDTO repositoryDTO = new RepositoryDTO(repo.getId(), repo.getName(), repo.getDescription(), user.getUsername(), true, false, null);

    	when(repositoryRepository.existsByNameAndOwnerId(anyString(),anyLong())).thenReturn(true);

        try {
        	repositoryService.createRepository(repositoryDTO);
        }
        catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}
                        
    	when(cacheService.getUserId(anyString())).thenReturn(12345L);
    	
    	when(repositoryRepository.existsByNameAndOwnerId(anyString(),anyLong())).thenReturn(false);

        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
                        
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        when(repositoryRepository.save(any())).thenReturn(repo);
        

        RepositoryDTO result = repositoryService.createRepository(repositoryDTO);
        assertEquals("TestRepo", result.getName());
        
        repositoryDTO.setForked(true);
        repositoryDTO.setParentRepositoryId(5L);
        
        repo.setForkedFrom(repo);
        
        result = repositoryService.createRepository(repositoryDTO);
        assertEquals("TestRepo", result.getName());              
    }
        
    @Test
    void testUpdateRepository() {

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
        
        RepositoryDTO repositoryDTO = new RepositoryDTO(repo.getId(), repo.getName(), repo.getDescription(), user.getUsername(), true, false, null);
                        
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        when(repositoryRepository.save(any())).thenReturn(repo);

        RepositoryDTO result = repositoryService.updateRepository(1L,repositoryDTO);
        assertEquals("TestRepo", result.getName());          
    }
        
    @Test
    void testDeleteRepository() {
                        
        when(repositoryRepository.existsById(anyLong())).thenReturn(false);
        
        try {
        	repositoryService.deleteRepository(1L);
        }
        catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}

        when(repositoryRepository.existsById(anyLong())).thenReturn(true);

    	repositoryService.deleteRepository(1L);       
    }
    
}
