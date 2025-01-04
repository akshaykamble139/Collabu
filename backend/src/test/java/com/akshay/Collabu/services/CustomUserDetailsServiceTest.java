package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;

import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.UserRepository;

class CustomUserDetailsServiceTest {

    Logger logger = LoggerFactory.getLogger(CustomUserDetailsServiceTest.class);
	
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService userDetailsService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testLoadUserByUsernameExisting() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setPassword("password!");
        mockUser.setRole("ROLE_USER");
        mockUser.setIsActive(true);
        
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.of(mockUser));

        UserDetails result = userDetailsService.loadUserByUsername("john_doe");

        assertEquals("john_doe", result.getUsername());
    }
    
    @Test
    void testLoadUserByUsernameNotExisting() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        Mockito.when(userRepository.findByUsername(anyString())).thenReturn(Optional.empty());

        try {
            userDetailsService.loadUserByUsername("john_doe");
		} catch (Exception e) {
			logger.info("used doesn't exist for username: {}", mockUser.getUsername());
		}

    }
    
}
