package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BranchDTO {
    private Long id;
    
    @NotBlank(message = "Branch name can't be empty")
    @Size(max = 100, message = "Branch name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Branch name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String name;
    
    @NotBlank(message = "Repository name can't be empty")
    @Size(max = 100, message = "Repository name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String repositoryName;
    
    private Boolean defaultBranch;
    
    private String parentBranchName; 
}
