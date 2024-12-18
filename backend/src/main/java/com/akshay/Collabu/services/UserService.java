package com.akshay.Collabu.services;

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
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setEmail(userDTO.getEmail());
        user.setPassword(userDTO.getPassword()); // Ensure this is hashed!
        User savedUser = userRepository.save(user);
        return new UserDTO(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
    }
}

