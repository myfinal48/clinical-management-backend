package com.clinicapp.backend.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String errorCode;
    private String message;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, String> details;
    private String path;
}
