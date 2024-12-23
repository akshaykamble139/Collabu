package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryDTO {
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
        
    private boolean isPublic = true;
    
    private boolean isForked = false;
    private Long parentRepositoryId; // ID of the parent repository if it's a fork
}
