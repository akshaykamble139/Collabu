package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDetailsDTO{

    private String username;
	private String email;
	private String password;
    private String bio;
    private String location;
    private String website;
    private String profilePicture;
    private LocalDateTime createdAt;
}
