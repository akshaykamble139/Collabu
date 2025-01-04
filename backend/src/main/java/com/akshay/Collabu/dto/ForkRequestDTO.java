package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForkRequestDTO {
	
    @NotNull(message = "repositoryId can't be null")
    private Long repositoryId;
    
    @NotBlank(message = "Repository name can't be empty")
    @Size(max = 100, message = "Repository name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String name;
}
