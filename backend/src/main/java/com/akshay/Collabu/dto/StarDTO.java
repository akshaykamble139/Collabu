package com.akshay.Collabu.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StarDTO {
    private Long id;
    
    @NotNull(message = "userId can't be null")
    private Long userId;

    @NotNull(message = "repositoryId can't be null")
    private Long repositoryId;
    
    private Boolean isActive;
}
