package com.akshay.Collabu.services;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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

import com.akshay.Collabu.dto.UserDTO;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.UserRepository;

class UserServiceTest {

	public static final Logger logger = LoggerFactory.getLogger(UserServiceTest.class);
	
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetUserById() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));

        UserDTO result = userService.getUserById(1L);

        assertEquals("john_doe", result.getUsername());
        assertEquals("john@gmail.com", result.getEmail());
    }
    
    @Test
    void testCreateUserThatAlreadyExists() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@gmail.com");
        
        Mockito.when(userRepository.existsByUsernameOrEmail(anyString(),anyString())).thenReturn(true);

        UserDTO result;
        try {
            result = userService.createUser(userDTO);
		} catch (Exception e) {
			logger.info("username or email already exists : {} {}", userDTO.getUsername(), userDTO.getEmail());
		}
    }
    
    @Test
    void testCreateUserNewUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@gmail.com");
                
        Mockito.when(userRepository.existsByUsernameOrEmail(anyString(),anyString())).thenReturn(false);
        Mockito.when(userRepository.save(any())).thenReturn(mockUser);

        UserDTO result = userService.createUser(userDTO);

        assertEquals("john_doe", result.getUsername());
        assertEquals("john@gmail.com", result.getEmail());
    }
        
    @Test
    void testDeleteNotExistingUser() {
                
        Mockito.when(userRepository.existsById(anyLong())).thenReturn(false);

        try {
        	userService.deleteUser(1L);
		} catch (Exception e) {
			logger.info("user doesn't exist for id: {}", 1L);
		}
    }
    
    @Test
    void testDeleteExistingUser() {

        Mockito.when(userRepository.existsById(anyLong())).thenReturn(true);
        
        userService.deleteUser(1L);
    }
    
    
    
    @Test
    void testupdateUserNotExistingUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@gmail.com");
                
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
        Mockito.when(userRepository.save(any())).thenReturn(mockUser);

        try {
        	UserDTO result = userService.updateUser(1L,userDTO);
		} catch (Exception e) {
			logger.info("user doesn't exist for id: {}", 1L);
		}
    }
    
    @Test
    void testupdateUserExistingUser() {
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("john_doe");
        mockUser.setEmail("john@gmail.com");
        mockUser.setIsActive(true);
        
        UserDTO userDTO = new UserDTO(1L, "john_doe", "john@gmail.com");
                
        Mockito.when(userRepository.findById(anyLong())).thenReturn(Optional.of(mockUser));
        Mockito.when(userRepository.save(any())).thenReturn(mockUser);

        UserDTO result = userService.updateUser(1L,userDTO);

        assertEquals("john_doe", result.getUsername());
        assertEquals("john@gmail.com", result.getEmail());
    }
}
