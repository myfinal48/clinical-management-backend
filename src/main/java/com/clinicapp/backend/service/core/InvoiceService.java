package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.model.core.Invoice;
import java.math.BigDecimal;

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
    BigDecimal getTotalPaidAmount();
    
    /**
     * Retrieves the full Invoice entity by ID (used for PDF generation).
     */
    Invoice getInvoiceEntityById(Long id);
} 