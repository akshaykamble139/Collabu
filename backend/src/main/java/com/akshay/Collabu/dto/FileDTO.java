package com.akshay.Collabu.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FileDTO implements Serializable{
    private static final long serialVersionUID = 2871096297612795781L;

	private Long id;
    
    @NotBlank(message = "File name can't be empty")
    @Size(max = 100, message = "File name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_.]*$", message = "File name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String name;
    
    private String content;
    
    @NotBlank(message = "Path can't be empty")
    private String path;          // New field for file path
    private String type = "file";
    
    @NotBlank(message = "Repository name can't be empty")
    @Size(max = 100, message = "Repository name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Repository name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String repositoryName;
    
    @NotBlank(message = "Branch name can't be empty")
    @Size(max = 100, message = "Branch name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_]*$", message = "Branch name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String branchName;
    
    private String commitMessage;
    
    private boolean isBinary;
    private String mimeType;

}