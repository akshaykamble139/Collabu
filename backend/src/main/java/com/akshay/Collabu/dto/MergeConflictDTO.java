package com.akshay.Collabu.dto;

import lombok.Data;

@Data
public class MergeConflictDTO {
    private Long id;
    private Long pullRequestId;
    private String conflictingFile;
    private String conflictDetails;
    private boolean resolved;
}

