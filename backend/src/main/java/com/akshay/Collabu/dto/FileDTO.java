package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDTO {
    private Long id;
    
    @NotBlank(message = "File name can't be empty")
    private String name;
    private String content;
    
    @NotBlank(message = "Path can't be empty")
    private String path;          // New field for file path
    private String type = "file";
    
    @NotNull(message = "Repository can't be null")
    private Long repositoryId;
}