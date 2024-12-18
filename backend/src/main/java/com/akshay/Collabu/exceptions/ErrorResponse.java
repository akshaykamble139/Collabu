package com.akshay.Collabu.exceptions;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Data;

@Data
public class ErrorResponse {
    private LocalDateTime timestamp;
    private String message;
    private Map<String, String> details;

    public ErrorResponse(String message, Map<String, String> details) {
        this.timestamp = LocalDateTime.now();
        this.message = message;
        this.details = details;
    }
}

