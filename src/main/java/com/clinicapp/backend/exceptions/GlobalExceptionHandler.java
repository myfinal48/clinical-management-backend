package com.clinicapp.backend.exceptions;

import com.clinicapp.backend.response.ErrorResponse;
import com.clinicapp.backend.util.PdfGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private ErrorResponse buildErrorResponse(HttpStatus status, String message, String path, Map<String, String> details, String errorCode) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .errorCode(errorCode)
                .message(message)
                .path(path)
                .details(details)
                .build();
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request.getDescription(false).replace("uri=", ""), null, null);
        return new ResponseEntity<>(errorResponse, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, ex.getMessage(), request.getDescription(false).replace("uri=", ""), null, null);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, WebRequest request) {
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.CONFLICT, ex.getMessage(), request.getDescription(false).replace("uri=", ""), null, null);
        return new ResponseEntity<>(errorResponse, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation failed", request.getDescription(false).replace("uri=", ""), errors, "VALIDATION_ERROR");
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        ErrorResponse response = buildErrorResponse(ex.getStatus(), ex.getMessage(), request.getDescription(false).replace("uri=", ""), null, ex.getErrorCode());
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(PdfGenerator.PdfGenerationException.class)
    public ResponseEntity<ErrorResponse> handlePdfError(PdfGenerator.PdfGenerationException ex, WebRequest request) {
        logger.error("PDF Generation Error: ", ex);
        ErrorResponse errorResponse = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "PDF generation error: " + ex.getMessage(), request.getDescription(false).replace("uri=", ""), null, "PDF_GENERATION_FAILED");
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAllExceptions(Exception ex, WebRequest request) {
        logger.error("An unexpected error occurred: ", ex);
        ErrorResponse response = buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred. Please contact support.", request.getDescription(false).replace("uri=", ""), null, "UNEXPECTED_ERROR");
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}