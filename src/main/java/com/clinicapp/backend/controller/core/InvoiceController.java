package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.service.core.InvoiceService;
import com.clinicapp.backend.service.core.HospitalInfoService;
import com.clinicapp.backend.util.PdfGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("${api.prefix}/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PdfGenerator pdfGenerator;
    private final HospitalInfoService hospitalInfoService;

    @PostMapping
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO created = invoiceService.createInvoice(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO updated = invoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
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

    /**
     * Génère un PDF de la facture pour impression/remise au patient
     */
    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('SECRETARY')")
    public ResponseEntity<InputStreamResource> generateInvoicePdf(@PathVariable Long id) {
        // Récupérer la facture
        InvoiceResponseDTO invoiceResponse = invoiceService.getInvoiceById(id);
        
        // Convertir en entité pour le PDF (on a besoin de l'entité complète)
        // Pour l'instant, on va utiliser le service pour récupérer l'entité
        // TODO: Optimiser en ajoutant une méthode dans le service pour récupérer l'entité
        var invoice = invoiceService.getInvoiceEntityById(id);
        var hospital = hospitalInfoService.getInfo();
        
        ByteArrayInputStream pdf = pdfGenerator.generateInvoice(invoice, hospital);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=facture_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
} 