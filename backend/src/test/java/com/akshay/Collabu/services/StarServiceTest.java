package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
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

import com.akshay.Collabu.dto.StarDTO;
import com.akshay.Collabu.models.Repository_;
import com.akshay.Collabu.models.Star;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.RepositoryRepository;
import com.akshay.Collabu.repositories.StarRepository;
import com.akshay.Collabu.repositories.UserRepository;

class StarServiceTest {
	
	public static final Logger logger = LoggerFactory.getLogger(StarServiceTest.class);

    @Mock
    private StarRepository starRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RepositoryRepository repositoryRepository;

    @InjectMocks
    private StarService starService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetStarCountByRepositoryId() {
        List<Star> stars = new ArrayList<>();
        Star star = new Star();
        star.setId(1L);
        star.setCreatedAt(LocalDateTime.now());
        star.setIsActive(true);
        
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
        
        star.setUser(user);
        star.setRepository(repo);
        
        stars.add(star);
        when(starRepository.findByRepositoryId(anyLong())).thenReturn(stars);

        Integer result = starService.getStarCountByRepositoryId(1L);
        assertEquals(1, result);
    }
    
    @Test
    void testToggleStar() {
        Star star = new Star();
        star.setId(1L);
        star.setCreatedAt(LocalDateTime.now());
        star.setIsActive(true);
        
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
        
        star.setUser(user);
        star.setRepository(repo);
        
        StarDTO starDTO = new StarDTO(star.getId(), star.getRepository().getId(), star.getIsActive());

        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
       
    	when(starRepository.findByUserIdAndRepositoryId(anyLong(),anyLong())).thenReturn(Optional.of(star));

        when(starRepository.save(any())).thenReturn(star);
        
        StarDTO result = starService.toggleStar(user.getUsername(),starDTO);
        assertEquals(false, result.getIsActive());
        
        result = starService.toggleStar(user.getUsername(),starDTO);
        assertEquals(true, result.getIsActive());
        
		when(starRepository.findByUserIdAndRepositoryId(anyLong(),anyLong())).thenReturn(Optional.empty());
        
        try {
            when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
            starService.toggleStar(user.getUsername(),starDTO);
        }
        catch (Exception e) {
			logger.info("User doesn't exist for id: {}", 1L);
		}
        
        try {
            when(userRepository.findById(anyLong())).thenReturn(Optional.of(user));
            when(repositoryRepository.findById(anyLong())).thenReturn(Optional.empty());
            starService.toggleStar(user.getUsername(),starDTO);
	    }
	    catch (Exception e) {
			logger.info("Repository doesn't exist for id: {}", 1L);
		}
        
        when(repositoryRepository.findById(anyLong())).thenReturn(Optional.of(repo));
        
        when(starRepository.save(any())).thenReturn(star);
        
        result = starService.toggleStar(user.getUsername(),starDTO);
        assertEquals(true, result.getIsActive());
    }
        
    @Test
    void testgetStarStatus() {
        Star star = new Star();
        star.setId(1L);
        star.setCreatedAt(LocalDateTime.now());
        star.setIsActive(true);
        
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
        
        star.setUser(user);
        star.setRepository(repo);
        
        StarDTO starDTO = new StarDTO(star.getId(), star.getRepository().getId(), star.getIsActive());
       
    	when(starRepository.findByUserIdAndRepositoryId(anyLong(),anyLong())).thenReturn(Optional.of(star));
        
        StarDTO result = starService.getStarStatus(user.getUsername(),starDTO);
        assertEquals(true, result.getIsActive());
        
		when(starRepository.findByUserIdAndRepositoryId(anyLong(),anyLong())).thenReturn(Optional.empty());
		
        result = starService.getStarStatus(user.getUsername(),starDTO);
        assertEquals(false, result.getIsActive());    
    }
    
	@Test
	void testDeleteStar() {
	                    
	    when(starRepository.existsById(anyLong())).thenReturn(false);
	    
	    try {
	    	starService.deleteStar(1L);
	    }
	    catch (Exception e) {
			logger.info("Star doesn't exist for id: {}", 1L);
		}
	
	    when(starRepository.existsById(anyLong())).thenReturn(true);
	
		starService.deleteStar(1L);       
	}
}
