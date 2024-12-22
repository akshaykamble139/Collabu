package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityLogDTO {
    private Long id;
    
    @NotNull(message = "User id can't be null")
    private Long userId;
    
    @NotNull(message = "Repository id can't be null")
    private Long repositoryId;
    
    private Long branchId;
    private Long fileId;
    
    @NotBlank(message = "Action can't be blank")
    private String action;
    
    private LocalDateTime timestamp;
}