package com.akshay.Collabu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileDTO {
    private Long id;
    private String name;
    private String content;
    private Long repositoryId;

}

