package com.akshay.Collabu.dto;

import lombok.Data;

@Data
public class UserDTO {
	private Long id;
    private String username;
    private String email;
    private String password;
    
	public UserDTO(Long id, String username, String email) {
		super();
		this.id = id;
		this.username = username;
		this.email = email;
	}
    
    
}

