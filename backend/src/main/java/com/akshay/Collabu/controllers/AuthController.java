package com.akshay.Collabu.controllers;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.akshay.Collabu.dto.UserDTO;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.services.CustomUserDetailsService;
import com.akshay.Collabu.services.UserService;
import com.akshay.Collabu.utils.JwtUtils;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private CustomUserDetailsService userDetailsService;
    
    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody @Valid UserDTO userDTO) {
        userDTO.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        userService.createUser(userDTO);
        return ResponseEntity.ok("User registered successfully!");
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserDTO userDTO) {
    	try {
    		UserDetails userDetails = userDetailsService.loadUserByUsername(userDTO.getUsername());
            if (!userDetails.isAccountNonLocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("Account inactive. Please activate your account");
            }
            if (passwordEncoder.matches(userDTO.getPassword(), userDetails.getPassword())) {
            	userService.updateLastLogin(userDTO.getUsername());
                String token = jwtUtils.generateToken(userDTO.getUsername());
                return ResponseEntity.ok(token);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
            }
		} catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
		}        
    }
    
    @PostMapping("/reactivate")
    public ResponseEntity<String> reactivateAccount(@RequestBody UserDTO userDTO) {
    	boolean result = userService.updateIsActive(userDTO);
        
        if (result) {
            return ResponseEntity.ok("Account reactivated successfully.");
        } else {
            return ResponseEntity.badRequest().body("Account is already active.");
        }
    }
}
