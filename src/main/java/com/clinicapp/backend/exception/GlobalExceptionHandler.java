package com.clinicapp.backend.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.clinicapp.backend.response.ErrorResponse;
import com.clinicapp.backend.util.PdfGenerator;

@RestControllerAdvice
public class GlobalExceptionHandler  extends ResponseEntityExceptionHandler {
    //méthode pour gérer les erreurs de validation
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<com.clinicapp.backend.response.ErrorResponse> handleApiException(ApiException ex, WebRequest request) {
        com.clinicapp.backend.response.ErrorResponse response = com.clinicapp.backend.response.ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(ex.getStatus().value())
                .error(ex.getStatus().getReasonPhrase())
                .errorCode(ex.getErrorCode())
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(response, ex.getStatus());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<com.clinicapp.backend.response.ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        com.clinicapp.backend.response.ErrorResponse response = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .error("Internal Server Error")
                .message(ex.getMessage())
                .path(request.getDescription(false).replace("uri=", ""))
                .build();
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(PdfGenerator.PdfGenerationException.class)
    public ResponseEntity<String> handlePdfError(PdfGenerator.PdfGenerationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("PDF generation error : " + ex.getMessage());
    }

}
