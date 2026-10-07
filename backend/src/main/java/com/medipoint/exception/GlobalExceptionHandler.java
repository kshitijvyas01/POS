package com.medipoint.exception;

import com.medipoint.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.CannotCreateTransactionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Object>> handleCustomException(CustomException ex) {
        logger.warn("Custom exception [{}]: {}", ex.getErrorCode(), ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error(ex.getMessage(), ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));

        logger.warn("Validation error: {}", errors);
        ApiResponse<Object> response = ApiResponse.error(errors.isEmpty() ? "Validation failed." : errors, "VALIDATION_ERROR");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiResponse<Object>> handleBadCredentials(BadCredentialsException ex) {
        logger.warn("Authentication failed: Bad credentials");
        ApiResponse<Object> response = ApiResponse.error("Invalid email or password.", "INVALID_CREDENTIALS");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Object>> handleAccessDenied(AccessDeniedException ex) {
        logger.warn("Access denied: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Access denied: You do not have permission to execute this operation.", "FORBIDDEN_ACCESS");
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<Object>> handleAuthenticationException(AuthenticationException ex) {
        logger.warn("Authentication exception: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Authentication failed: " + ex.getMessage(), "UNAUTHORIZED");
        return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<Object>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        logger.error("Database constraint violation: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Database error: A record with this information already exists or violates constraints.", "DUPLICATE_RESOURCE");
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(CannotCreateTransactionException.class)
    public ResponseEntity<ApiResponse<Object>> handleDatabaseConnectionException(CannotCreateTransactionException ex) {
        logger.error("Database connectivity error: {}", ex.getMessage());
        ApiResponse<Object> response = ApiResponse.error("Database connection error: Unable to connect to MySQL database. Please verify the MySQL service is running and configured correctly.", "DATABASE_CONNECTION_ERROR");
        return new ResponseEntity<>(response, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleGeneralException(Exception ex) {
        logger.error("Unexpected server error: ", ex);
        ApiResponse<Object> response = ApiResponse.error("An unexpected server error occurred. Please try again later.", "INTERNAL_SERVER_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
