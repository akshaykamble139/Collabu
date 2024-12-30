package com.akshay.Collabu.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DiffDTO {
    private Long id;
    
    @NotNull(message = "file id can't be null")
    private Long fileId;
    
    @NotNull(message = "from version number ca't be null")
    private Integer fromVersion;

    @NotNull(message = "to version number ca't be null")
    private Integer toVersion;
    private String diffContent;
}
