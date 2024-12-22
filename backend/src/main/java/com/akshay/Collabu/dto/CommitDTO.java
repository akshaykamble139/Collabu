package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommitDTO {
    private Long id;
    private String message;
    private LocalDateTime timestamp;
    
    @NotNull(message = "Repository id can't be null")
    private Long repositoryId;

    @NotNull(message = "Branch id can't be null")
    private Long branchId;
}
