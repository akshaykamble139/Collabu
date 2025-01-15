package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateFileRequestDTO {
	
    private Long fileId;

    @NotBlank(message = "File name can't be empty")
    @Size(max = 100, message = "File name should not exceed 100 characters")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9_.]*$", message = "File name must start with an alphabet and can contain alphanumeric characters or underscores")
    private String fileName;
    
    @NotBlank(message = "Path can't be empty")
    @Pattern(regexp =  "^/(?:[a-zA-Z][a-zA-Z0-9_.-]*/)*$", 
    message = "File path must start and end with a slash that between any two slashes it starts with alphabet and can contain alphanumeric characters, underscores, dots.")
    private String filePath;    
    
    private String fileContent;
    private String contentType;
    private String message;
}
