package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BranchDTO {
    private Long id;
    
    @NotBlank(message = "Branch name can't be empty")
    private String name;
    
    @NotNull(message = "Repository id for this branch can't be null")
    private Long repositoryId;
}
