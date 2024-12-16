package com.akshay.Collabu.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RepositoryDTO {
    private Long id;
    private String name;
    private String description;
    private Long ownerId;

}

