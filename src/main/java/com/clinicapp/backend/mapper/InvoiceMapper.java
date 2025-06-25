package com.clinicapp.backend.mapper;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.dto.core.InvoicePatientDTO;
import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.Patient;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class InvoiceMapper {
    public Invoice toEntity(InvoiceRequestDTO dto, Patient patient) {
        return Invoice.builder()
                .patient(patient)
                .amount(dto.getAmount())
                .issuedAt(LocalDateTime.now())
                .paid(false)
                .description(dto.getDescription())
                .build();
    }

    public void updateEntity(Invoice invoice, InvoiceRequestDTO dto, Patient patient) {
        invoice.setPatient(patient);
        invoice.setAmount(dto.getAmount());
        invoice.setDescription(dto.getDescription());
    }

    public InvoiceResponseDTO toResponseDTO(Invoice invoice) {
        InvoicePatientDTO patientDTO = InvoicePatientDTO.builder()
                .id(invoice.getPatient().getId())
                .firstName(invoice.getPatient().getFirstName())
                .lastName(invoice.getPatient().getLastName())
                .build();
        return InvoiceResponseDTO.builder()
                .id(invoice.getId())
                .patient(patientDTO)
                .amount(invoice.getAmount())
                .issuedAt(invoice.getIssuedAt())
                .paid(invoice.isPaid())
                .description(invoice.getDescription())
                .datePaid(invoice.getDatePaid())
                .build();
    }
} 