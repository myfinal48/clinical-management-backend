package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.dto.core.PrescriptionResponseDto;
import com.clinicapp.backend.mapper.PrescriptionMapper;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.response.ApiResponse;
import com.clinicapp.backend.service.core.HospitalInfoService;
import com.clinicapp.backend.service.core.PrescriptionService;
import com.clinicapp.backend.util.PdfGenerator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

/**
 * Controller for managing patient prescriptions.
 * Provides endpoints for creating, retrieving, updating, deleting, and generating PDFs for prescriptions.
 */
@Tag(name = "prescription-controller", description = "Endpoints for managing patient prescriptions.")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PdfGenerator pdfGenerator;
    private final HospitalInfoService hospitalInfoService;

    /**
     * Retrieves a list of all prescriptions.
     * Accessible by users with DOCTOR or ADMIN roles.
     *
     * @return A list of all prescriptions.
     */
    @Operation(summary = "Get all prescriptions. Roles: DOCTOR, ADMIN.")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<PrescriptionResponseDto>> getAllPrescriptions() {
        List<PrescriptionResponseDto> dtos = prescriptionService.getAll().stream()
                .map(PrescriptionMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves prescriptions authored by a specific doctor (medecin).
     * Accessible by users with the DOCTOR role.
     *
     * @param id The doctor's (medecin) ID.
     * @return A list of prescriptions for the given doctor.
     */
    @Operation(summary = "Get prescriptions by doctor ID. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/doctor/{id}")
    public ResponseEntity<List<PrescriptionResponseDto>> getPrescriptionsByDoctor(@PathVariable Long id) {
        List<PrescriptionResponseDto> dtos = prescriptionService.getByMedecinId(id).stream()
                .map(PrescriptionMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    /**
     * Retrieves a specific prescription by its ID.
     * Accessible by users with the DOCTOR role.
     *
     * @param id The ID of the prescription to retrieve.
     * @return The prescription with the specified ID.
     */
    @Operation(summary = "Get a prescription by ID. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        PrescriptionResponseDto dto = PrescriptionMapper.toDto(prescription);
        return ResponseEntity.ok(new ApiResponse("Prescription found", dto));
    }

    /**
     * Creates a new prescription.
     * Accessible by users with the DOCTOR role.
     *
     * @param dto The prescription data to create.
     * @return The created prescription.
     */
    @Operation(summary = "Create a new prescription. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPrescription(
            @Valid @RequestBody PrescriptionCreationRequestDto dto) {

        Prescription created = prescriptionService.create(dto);
        PrescriptionResponseDto responseDto = PrescriptionMapper.toDto(created);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse("Prescription created successfully", responseDto));
    }

    /**
     * Updates an existing prescription by its ID.
     * Accessible by users with the DOCTOR role.
     *
     * @param id The ID of the prescription to update.
     * @param dto The updated prescription data.
     * @return The updated prescription.
     */
    @Operation(summary = "Update a prescription by ID. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreationRequestDto dto) {

        Prescription updated = prescriptionService.update(id, dto);
        PrescriptionResponseDto responseDto = PrescriptionMapper.toDto(updated);

        return ResponseEntity.ok(new ApiResponse("Prescription updated successfully", responseDto));
    }

    /**
     * Deletes a prescription by its ID.
     * Accessible by users with the DOCTOR role.
     *
     * @param id The ID of the prescription to delete.
     * @return A confirmation message.
     */
    @Operation(summary = "Delete a prescription by ID. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deletePrescription(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Prescription deleted successfully", null));
    }

    /**
     * Generates a PDF for a specific prescription.
     * Accessible by users with the DOCTOR role.
     *
     * @param id The ID of the prescription.
     * @return A PDF file of the prescription.
     */
    @Operation(summary = "Generate a PDF for a prescription. Role: DOCTOR.")
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/{id}/pdf")
    public ResponseEntity<InputStreamResource> generatePrescriptionPdf(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        var hospital = hospitalInfoService.getInfo();
        ByteArrayInputStream pdf = pdfGenerator.generate(prescription, hospital);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION,
                "attachment; filename=prescription_" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.APPLICATION_PDF)
                .body(new InputStreamResource(pdf));
    }
}