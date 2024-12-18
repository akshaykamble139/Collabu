package com.akshay.Collabu.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserDTO {
	private Long id;
	
	@NotBlank(message = "Username is required")
	@Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
    private String username;

	@NotBlank(message = "Email is required")
	@Email(message = "Invalid email format")
	private String email;

	@NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must have at least 6 characters")
	private String password;

	public UserDTO(Long id, String username, String email) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
	}
    
    
}

