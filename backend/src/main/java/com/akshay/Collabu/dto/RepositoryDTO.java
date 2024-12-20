package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryDTO {
    private Long id;
    
    @NotBlank(message = "Repository name can't be empty")
    private String name;
    
    private String description;
    
    @NotNull(message = "User ID can't be null")
    private Long ownerId;
    
    private boolean isPublic = true;
    
    private boolean isForked = false;
    private Long parentRepositoryId; // ID of the parent repository if it's a fork
}
