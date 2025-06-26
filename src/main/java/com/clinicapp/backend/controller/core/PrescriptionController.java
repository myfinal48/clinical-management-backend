package com.clinicapp.backend.controller.core;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.clinicapp.backend.dto.core.PrescriptionCreationRequestDto;
import com.clinicapp.backend.dto.core.PrescriptionResponseDto;
import com.clinicapp.backend.exceptions.ApiException;
import com.clinicapp.backend.model.core.Prescription;
import com.clinicapp.backend.response.ApiResponse;
import com.clinicapp.backend.service.core.PrescriptionService;
import com.clinicapp.backend.util.PdfGenerator;
import com.clinicapp.backend.service.core.HospitalInfoService;
import com.clinicapp.backend.mapper.PrescriptionMapper;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/v1/prescriptions")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;
    private final PdfGenerator pdfGenerator;
    private final HospitalInfoService hospitalInfoService;

    public PrescriptionController(PrescriptionService prescriptionService,
                                  PdfGenerator pdfGenerator,
                                  HospitalInfoService hospitalInfoService) {
        this.prescriptionService = prescriptionService;
        this.pdfGenerator = pdfGenerator;
        this.hospitalInfoService = hospitalInfoService;
    }

    @Operation(summary = "Get all prescriptions")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "List of prescriptions")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<PrescriptionResponseDto>> getAllPrescriptions() {
        List<Prescription> prescriptions = prescriptionService.getAll();
        List<PrescriptionResponseDto> dtos = prescriptions.stream()
            .map(PrescriptionMapper::toDto)
            .toList();
        return ResponseEntity.ok(dtos);
    }

    @Operation(summary = "Get prescription by ID")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription found")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    @PreAuthorize("hasRole('DOCTOR')")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse> getPrescriptionById(@PathVariable Long id) {
        Prescription prescription = prescriptionService.getById(id);
        PrescriptionResponseDto dto = convertToDto(prescription);
        return ResponseEntity.ok(new ApiResponse("Prescription trouvée", dto));
    }


    @Operation(summary = "Create new prescription")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Prescription created")
    @PreAuthorize("hasRole('DOCTOR')")
    @PostMapping("/create")
    public ResponseEntity<ApiResponse> createPrescription(
            @Valid @RequestBody PrescriptionCreationRequestDto dto) {

        Prescription created = prescriptionService.create(dto);
        PrescriptionResponseDto responseDto = convertToDto(created);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(created.getId())
                .toUri();

        return ResponseEntity.created(location)
                .body(new ApiResponse("Prescription créée avec succès", responseDto));
    }


    @Operation(summary = "Update prescription")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Prescription updated")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    @PreAuthorize("hasRole('DOCTOR')")
    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse> updatePrescription(
            @PathVariable Long id,
            @Valid @RequestBody PrescriptionCreationRequestDto dto) {

        Prescription updated = prescriptionService.update(id, dto);
        PrescriptionResponseDto responseDto = convertToDto(updated);

        return ResponseEntity.ok(new ApiResponse("Prescription mise à jour avec succès", responseDto));
    }

 
    @Operation(summary = "Delete prescription")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Prescription deleted")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
    @PreAuthorize("hasRole('DOCTOR')")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse> deletePrescription(@PathVariable Long id) {
        prescriptionService.delete(id);
        return ResponseEntity.ok(new ApiResponse("Prescription supprimée avec succès", null));
    }


    @Operation(summary = "Generate PDF for prescription")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "PDF generated successfully")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Prescription not found")
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

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<String> handleNotFound(ApiException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    private PrescriptionResponseDto convertToDto(Prescription prescription) {
        return PrescriptionMapper.toDto(prescription);
    }
}

