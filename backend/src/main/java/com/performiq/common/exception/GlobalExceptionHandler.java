package com.performiq.common.exception;

import com.performiq.common.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                ErrorResponse.builder()
                        .status(HttpStatus.NOT_FOUND.value())
                        .error(HttpStatus.NOT_FOUND.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, HttpServletRequest request) {
        log.warn("Bad request: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @ExceptionHandler(DataImportException.class)
    public ResponseEntity<ErrorResponse> handleDataImportException(DataImportException ex, HttpServletRequest request) {
        log.error("Data import error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNPROCESSABLE_ENTITY.value())
                        .error("Data Import Failure")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .toList();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.builder()
                        .status(HttpStatus.BAD_REQUEST.value())
                        .error("Validation Failed")
                        .message("Input validation constraints were violated")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .details(details)
                        .build()
        );
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                ErrorResponse.builder()
                        .status(HttpStatus.UNAUTHORIZED.value())
                        .error(HttpStatus.UNAUTHORIZED.getReasonPhrase())
                        .message("Invalid username or password")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
                ErrorResponse.builder()
                        .status(HttpStatus.FORBIDDEN.value())
                        .error(HttpStatus.FORBIDDEN.getReasonPhrase())
                        .message("Access denied: insufficient permissions")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled server exception: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ErrorResponse.builder()
                        .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                        .error(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase())
                        .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected server error occurred")
                        .path(request.getRequestURI())
                        .timestamp(Instant.now())
                        .build()
        );
    }
}
