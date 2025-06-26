package com.clinicapp.backend.dto.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponseDTO {
    private Long id;
    private InvoicePatientDTO patient;
    private BigDecimal amount;
    private LocalDateTime issuedAt;
    private boolean paid;
    private String description;
    private LocalDateTime datePaid;
} 