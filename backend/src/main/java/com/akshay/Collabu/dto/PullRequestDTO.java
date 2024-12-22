package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PullRequestDTO {
    private Long id;
    
    @NotBlank(message = "Title can't be blank")
    private String title;
    private String description;

    @NotBlank(message = "Status can't be blank")
    private String status;
    
    @NotNull(message = "Repository id can't be null")
    private Long repositoryId;

    @NotNull(message = "Source branch id can't be null")
    private Long sourceBranchId;
    
    @NotNull(message = "Target branch id can't be null")
    private Long targetBranchId;
    
    @NotNull(message = "Used id can't be null")
    private Long createdByUserId;
    
    private LocalDateTime createdAt;
}
