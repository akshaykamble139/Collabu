package com.akshay.Collabu.exceptions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Handle validation errors
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
	    Map<String, String> errors = new HashMap<>();

	    ex.getBindingResult().getAllErrors().forEach((error) -> {
	        String fieldName = ((FieldError) error).getField();
	        String errorMessage = error.getDefaultMessage();
	        errors.put(fieldName, errorMessage);
	    });

	    ErrorResponse errorResponse = new ErrorResponse("Validation failed", errors);
	    return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
	}
	
	@ExceptionHandler(SignatureException.class)
    public ResponseEntity<ErrorResponse> handleInvalidJwt(SignatureException ex) {
		Map<String, String> errors = new HashMap<>();
		errors.put("error", "Invalid token");
		errors.put("message", "Your session has expired or the token is invalid. Please login again.");

	    ErrorResponse errorResponse = new ErrorResponse("Invalid token", errors);
	    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ExpiredJwtException ex) {        
        Map<String, String> errors = new HashMap<>();
		errors.put("error", "Token expired");
		errors.put("message", "Your session has expired. Please login again.");

	    ErrorResponse errorResponse = new ErrorResponse("Token expired", errors);
	    return new ResponseEntity<>(errorResponse, HttpStatus.UNAUTHORIZED);
    }
    
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleExpiredJwt(ResponseStatusException ex) {        
        Map<String, String> errors = new HashMap<>();
        String[] messageArray = ex.getMessage().replace("\"", "").split(" ");
        
        StringBuilder message = new StringBuilder();
        
        for (int i=2; i < messageArray.length; i++) {
			message.append(messageArray[i] + " ");
			
		}
		errors.put("error", messageArray[1]);
		errors.put("message", message.toString());

	    ErrorResponse errorResponse = new ErrorResponse(message.toString(), errors);
	    return new ResponseEntity<>(errorResponse, HttpStatusCode.valueOf(Integer.valueOf(messageArray[0])));
    }


    // Handle other exceptions (generic example)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

