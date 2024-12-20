package com.akshay.Collabu.services;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.akshay.Collabu.dto.UserDTO;
import com.akshay.Collabu.models.User;
import com.akshay.Collabu.repositories.UserRepository;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

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
}


