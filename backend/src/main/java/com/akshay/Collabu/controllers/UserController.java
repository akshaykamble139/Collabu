package com.akshay.Collabu.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.akshay.Collabu.dto.UserDTO;
import com.akshay.Collabu.dto.UserDetailsDTO;
import com.akshay.Collabu.services.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
public class UserController {
	
	public static final Logger logger = LoggerFactory.getLogger(UserController.class);
	
    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUserById(@PathVariable Long id) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @GetMapping("/profile/{userName}")
    public ResponseEntity<UserDetailsDTO> getUserByUserName(@PathVariable String userName) {
        UserDetailsDTO user = userService.getUserDetailsByUserName(userName);
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/profile/update")
    public ResponseEntity<UserDetailsDTO> updateUserDetails(@RequestBody UserDetailsDTO userDetailsDTO, @AuthenticationPrincipal UserDetails userDetails) {
        UserDetailsDTO user = userService.updateUserDetails(userDetails, userDetailsDTO);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody UserDetailsDTO userDetailsDTO, @AuthenticationPrincipal UserDetails userDetails) {
        boolean success = userService.updatePassword(userDetails, userDetailsDTO);
        return ResponseEntity.ok("Password changed successfully!");
    }

    @PostMapping
    public ResponseEntity<UserDTO> createUser(@RequestBody @Valid UserDTO userDTO) {
        UserDTO createdUser = userService.createUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }
    
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@AuthenticationPrincipal UserDetails userDetails) {
        userService.deleteUserByUsername(userDetails.getUsername());
        return ResponseEntity.status(HttpStatusCode.valueOf(204)).body("Account deleted succesfully!");
    }
    
    @PostMapping("/upload-profile-picture")
    public ResponseEntity<?> uploadProfilePicture(@RequestParam("file") MultipartFile file, @AuthenticationPrincipal UserDetails userDetails) {
        if (file.isEmpty()) {
            return new ResponseEntity<>("File is empty", HttpStatus.BAD_REQUEST);
        }

        try {
        	
        	Boolean result = userService.uploadProfilePicture(userDetails, file);
        	
        	if (result) {
        		return ResponseEntity.ok().body("Profile picture uploaded successfully.");
        	}
            return new ResponseEntity<>("Could not upload file", HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
        	logger.error(e.getMessage());
        	e.printStackTrace();
            return new ResponseEntity<>("Could not upload file", HttpStatus.INTERNAL_SERVER_ERROR);
		}
    }
}

