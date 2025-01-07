package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class StarDTO {
    private Long id;
    
    @NotBlank(message = "Repository owner username can't be null")
	@Size(min = 3, max = 20, message = "Repository owner username must be between 3 and 20 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository owner username must start with an alphabet and can contain alphanumeric characters or underscores")
    private String ownerUsername;
    
    @NotBlank(message = "Repository name can't be empty")
    @Size(max = 100, message = "Repository name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String repositoryName;
    
    private Boolean isActive;
}
