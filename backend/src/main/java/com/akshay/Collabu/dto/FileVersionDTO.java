package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class FileVersionDTO {
    private Long id;
    
    @NotNull(message = "file if can't be null")
    private Long fileId;
    private int versionNumber;
    private String hash;
    private LocalDateTime createdAt;
}
