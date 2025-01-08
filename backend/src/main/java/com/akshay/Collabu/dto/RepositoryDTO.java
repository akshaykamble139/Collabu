package com.akshay.Collabu.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RepositoryDTO implements Serializable {
    private static final long serialVersionUID = 2745070311177804504L;

	private Long id;
    
    @NotBlank(message = "Repository name can't be empty")
    @Size(max = 100, message = "Repository name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String name;
    
    private String description;

    @NotBlank(message = "Repository owner username can't be null")
	@Size(min = 3, max = 20, message = "Repository owner username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository owner username must start with an alphabet and can contain alphanumeric characters or underscores")
    private String ownerUsername;
        
    private boolean publicRepositoryOrNot = true;
    
    private boolean repositoryForkedOrNot = false;
    private Long parentRepositoryId; // ID of the parent repository if it's a fork
    
    private Long starCount = 0L;
    private Long forkCount = 0L;
}
