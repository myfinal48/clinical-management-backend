package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.annotation.Auditable;
import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.service.core.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("${api.prefix}/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasRole('SECRETARY')")
    @Auditable(action = "CREATE_INVOICE", entityType = "INVOICE", logParameters = true)
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO created = invoiceService.createInvoice(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    @Auditable(action = "UPDATE_INVOICE", entityType = "INVOICE", logParameters = true)
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO updated = invoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    @Auditable(action = "DELETE_INVOICE", entityType = "INVOICE", logParameters = true, logResult = false)
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    @GetMapping("/patient/{id}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByPatient(@PathVariable Long id) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByPatient(id);
        return ResponseEntity.ok(invoices);
    }

    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('SECRETARY')")
    @Auditable(action = "MARK_INVOICE_PAID", entityType = "INVOICE", logParameters = true)
    public ResponseEntity<InvoiceResponseDTO> markAsPaid(@PathVariable Long id) {
        InvoiceResponseDTO paidInvoice = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(paidInvoice);
    }

    @GetMapping("/paid")
    public ResponseEntity<List<InvoiceResponseDTO>> getPaidInvoices() {
        return ResponseEntity.ok(invoiceService.getPaidInvoices());
    }

    @GetMapping("/unpaid")
    public ResponseEntity<List<InvoiceResponseDTO>> getUnpaidInvoices() {
        return ResponseEntity.ok(invoiceService.getUnpaidInvoices());
    }

    @GetMapping("/total-paid")
    public ResponseEntity<java.math.BigDecimal> getTotalPaidAmount() {
        return ResponseEntity.ok(invoiceService.getTotalPaidAmount());
    }
} 