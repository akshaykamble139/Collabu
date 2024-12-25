package com.akshay.Collabu.services;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.akshay.Collabu.dto.UserDTO;
import com.akshay.Collabu.dto.UserDetailsDTO;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private CacheService cacheService;

	@Autowired
	private Environment env;
	
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return new UserDTO(user.getId(), user.getUsername(), user.getEmail());
    }

    public UserDTO createUser(UserDTO userDTO) {        
        if (userRepository.existsByUsernameOrEmail(userDTO.getUsername(), userDTO.getEmail())) {
            throw new RuntimeException("Username or email already exists");
        }
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); 
        user.setRole("ROLE_USER"); // Default role
        user.setCreatedAt(LocalDateTime.now());
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("User not found");
        }
        userRepository.deleteById(id);
    }

    public UserDTO updateUser(Long id, UserDTO userDTO) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        User updatedUser = userRepository.save(user);
        return new UserDTO(updatedUser.getId(), updatedUser.getUsername(), updatedUser.getEmail());
    }
    
    public Boolean uploadProfilePicture(UserDetails userDetails, MultipartFile file) throws IllegalStateException, IOException {
    	// Get current authenticated user
        User user = userRepository.findByUsername(userDetails.getUsername())
        			.orElseThrow(() -> new RuntimeException("User not found"));

        // Create directory if not exists
        String uploadDirectory = env.getProperty("profile.image.upload-dir", "src/main/resources/static/images/profile_pictures/");

        Path uploadPath = Paths.get(uploadDirectory);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save the image with username
        String fileName = user.getUsername() + "_profile_pic.jpg";
        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        // Save URL in database
        String fileUrl = "/static/images/" + fileName;
        user.setProfilePicture(fileUrl);
        userRepository.save(user);
        
        return true;
	}

	public UserDetailsDTO getUserDetailsByUserName(String userName) {
		User user = userRepository.findByUsername(userName)
    			.orElseThrow(() -> new RuntimeException("User not found"));
        return mapEntityToDto(user);
	}

	private UserDetailsDTO mapEntityToDto(User user) {
		UserDetailsDTO userDetailsDTO = new UserDetailsDTO();
        
        userDetailsDTO.setUsername(user.getUsername());
        userDetailsDTO.setEmail(user.getEmail());
        userDetailsDTO.setBio(user.getBio());
        userDetailsDTO.setProfilePicture(user.getProfilePicture());
        userDetailsDTO.setWebsite(user.getWebsite());
        userDetailsDTO.setLocation(user.getLocation());
        userDetailsDTO.setCreatedAt(user.getCreatedAt());
        
        return userDetailsDTO;
	}

	public UserDetailsDTO updateUserDetails(UserDetails userDetails, UserDetailsDTO userDetailsDTO) {
		User user = userRepository.findByUsername(userDetails.getUsername())
    			.orElseThrow(() -> new RuntimeException("User not found"));
		
		if (userDetailsDTO.getBio() != null && !userDetailsDTO.getBio().isBlank()) {
			user.setBio(userDetailsDTO.getBio());
		}
		
		if (userDetailsDTO.getLocation() != null && !userDetailsDTO.getLocation().isBlank()) {
			user.setBio(userDetailsDTO.getLocation());
		}
		
		if (userDetailsDTO.getWebsite() != null && !userDetailsDTO.getWebsite().isBlank()) {
			user.setBio(userDetailsDTO.getWebsite());
		}
	
        User updatedUser = userRepository.save(user);
                
        return mapEntityToDto(updatedUser);

	}

	public boolean updatePassword(UserDetails userDetails, UserDetailsDTO userDetailsDTO) {
		User user = userRepository.findByUsername(userDetails.getUsername())
    			.orElseThrow(() -> new RuntimeException("User not found"));
		
		String password = userDetailsDTO.getPassword();
        user.setPassword(passwordEncoder.encode(password));

        userRepository.save(user);
        
		return true;
		
	}
	
	public void updateLastLogin(String username) {
		User user = userRepository.findByUsername(username)
    			.orElseThrow(() -> new RuntimeException("User not found"));
		
		user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);		
	}

	public void deleteUserByUsername(String username) {
    	Long userId = cacheService.getUserId(username);
    	if (userId == null) {
            throw new RuntimeException("User id doesn't exist for this username");    		
    	}
    	deleteUser(userId);
	}
}


