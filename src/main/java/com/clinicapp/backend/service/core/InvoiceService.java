package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;

import java.util.List;

public interface InvoiceService {
    InvoiceResponseDTO createInvoice(InvoiceRequestDTO invoiceRequestDTO);
    InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO invoiceRequestDTO);
    void deleteInvoice(Long id);
    InvoiceResponseDTO getInvoiceById(Long id);
    List<InvoiceResponseDTO> getAllInvoices();
    List<InvoiceResponseDTO> getInvoicesByPatient(Long patientId);
    InvoiceResponseDTO markAsPaid(Long id);
    List<InvoiceResponseDTO> getPaidInvoices();
    List<InvoiceResponseDTO> getUnpaidInvoices();
    java.math.BigDecimal getTotalPaidAmount();
} 