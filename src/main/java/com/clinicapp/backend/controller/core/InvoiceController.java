package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.service.core.InvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
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

/**
 * Controller for managing patient invoices.
 * Provides endpoints for creating, retrieving, updating, and deleting invoices, as well as managing their payment status.
 */
@Tag(name = "invoice-controller", description = "Endpoints for managing patient invoices.")
@RestController
@RequestMapping("${api.prefix}/invoices")
@RequiredArgsConstructor
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final PdfGenerator pdfGenerator;
    private final HospitalInfoService hospitalInfoService;

    /**
     * Creates a new invoice.
     * Accessible only by users with the SECRETARY role.
     *
     * @param dto The invoice data to create.
     * @return The created invoice.
     */
    @Operation(summary = "Create a new invoice. Role: SECRETARY.")
    @PostMapping
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<InvoiceResponseDTO> createInvoice(@RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO created = invoiceService.createInvoice(dto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    /**
     * Updates an existing invoice by its ID.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the invoice to update.
     * @param dto The updated invoice data.
     * @return The updated invoice.
     */
    @Operation(summary = "Update an invoice by ID. Role: SECRETARY.")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<InvoiceResponseDTO> updateInvoice(@PathVariable Long id, @RequestBody InvoiceRequestDTO dto) {
        InvoiceResponseDTO updated = invoiceService.updateInvoice(id, dto);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes an invoice by its ID.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the invoice to delete.
     * @return A success response with no content.
     */
    @Operation(summary = "Delete an invoice by ID. Role: SECRETARY.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Retrieves a specific invoice by its ID.
     *
     * @param id The ID of the invoice to retrieve.
     * @return The invoice with the specified ID.
     */
    @Operation(summary = "Get an invoice by ID. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponseDTO> getInvoiceById(@PathVariable Long id) {
        InvoiceResponseDTO invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    /**
     * Retrieves a list of all invoices.
     *
     * @return A list of all invoices.
     */
    @Operation(summary = "Get all invoices. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping
    public ResponseEntity<List<InvoiceResponseDTO>> getAllInvoices() {
        List<InvoiceResponseDTO> invoices = invoiceService.getAllInvoices();
        return ResponseEntity.ok(invoices);
    }

    /**
     * Retrieves all invoices for a specific patient.
     *
     * @param id The ID of the patient.
     * @return A list of invoices for the specified patient.
     */
    @Operation(summary = "Get all invoices for a specific patient. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/patient/{id}")
    public ResponseEntity<List<InvoiceResponseDTO>> getInvoicesByPatient(@PathVariable Long id) {
        List<InvoiceResponseDTO> invoices = invoiceService.getInvoicesByPatient(id);
        return ResponseEntity.ok(invoices);
    }

    /**
     * Marks an invoice as paid.
     * Accessible only by users with the SECRETARY role.
     *
     * @param id The ID of the invoice to mark as paid.
     * @return The updated invoice, marked as paid.
     */
    @Operation(summary = "Mark an invoice as paid. Role: SECRETARY.")
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasRole('SECRETARY')")
    public ResponseEntity<InvoiceResponseDTO> markAsPaid(@PathVariable Long id) {
        InvoiceResponseDTO paidInvoice = invoiceService.markAsPaid(id);
        return ResponseEntity.ok(paidInvoice);
    }

    /**
     * Retrieves a list of all paid invoices.
     *
     * @return A list of paid invoices.
     */
    @Operation(summary = "Get all paid invoices. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/paid")
    public ResponseEntity<List<InvoiceResponseDTO>> getPaidInvoices() {
        return ResponseEntity.ok(invoiceService.getPaidInvoices());
    }

    /**
     * Retrieves a list of all unpaid invoices.
     *
     * @return A list of unpaid invoices.
     */
    @Operation(summary = "Get all unpaid invoices. role: ADMIN, DOCTOR, SECRETARY")
    @GetMapping("/unpaid")
    public ResponseEntity<List<InvoiceResponseDTO>> getUnpaidInvoices() {
        return ResponseEntity.ok(invoiceService.getUnpaidInvoices());
    }

    /**
     * Calculates the total amount of all paid invoices.
     *
     * @return The total paid amount.
     */
    @Operation(summary = "Get the total amount of all paid invoices. role: ADMIN, DOCTOR, SECRETARY")
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
        InvoiceResponseDTO invoiceResponse = invoiceService.getInvoiceById(id);
        

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
