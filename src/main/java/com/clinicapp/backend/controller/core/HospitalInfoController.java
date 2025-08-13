package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.model.core.HospitalInfo;
import com.clinicapp.backend.service.core.HospitalInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Controller for managing hospital information.
 * Provides endpoints for creating, retrieving, updating, and deleting hospital details and logo.
 * Access is restricted to users with the ADMIN role.
 */
@Tag(name = "hospital-info-controller", description = "Endpoints for managing hospital information (Admin access only).")
@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/hospital")
@PreAuthorize("hasRole('ADMIN')")
public class HospitalInfoController {
    private final HospitalInfoService service;

    /**
     * Retrieves all hospital information records.
     *
     * @return A list of hospital information.
     */
    @Operation(summary = "Get all hospital information. Role: ADMIN.")
    @GetMapping("/info")
    public ResponseEntity<List<HospitalInfo>> getInfo() {
        return ResponseEntity.ok(service.getAllInfo());
    }

    /**
     * Saves new hospital information along with a logo.
     *
     * @param name    The name of the hospital.
     * @param address The address of the hospital.
     * @param phone   The phone number of the hospital.
     * @param email   The email address of the hospital.
     * @param logo    The hospital's logo file.
     * @return The saved hospital information.
     * @throws Exception if there is an error during file processing.
     */
    @Operation(summary = "Save new hospital information and logo. Role: ADMIN.")
    @PostMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HospitalInfo> saveInfoAndLogo(
            @Parameter(description = "Hospital name") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Hospital address") @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Hospital phone number") @RequestParam(value = "phone", required = false) String phone,
            @Parameter(description = "Hospital email") @RequestParam(value = "email", required = false) String email,
            @Parameter(description = "Logo file (JPG, PNG, JPEG, SVG)") @RequestPart(value = "logo", required = false) MultipartFile logo) throws Exception {
        return ResponseEntity.ok(service.saveInfoAndLogo(name, address, phone, email, logo));
    }

    /**
     * Updates existing hospital information and logo by ID.
     *
     * @param id      The ID of the hospital information to update.
     * @param name    The new name of the hospital.
     * @param address The new address of the hospital.
     * @param phone   The new phone number of the hospital.
     * @param email   The new email address of the hospital.
     * @param logo    The new logo file for the hospital.
     * @return The updated hospital information.
     * @throws Exception if there is an error during file processing.
     */
    @Operation(summary = "Update hospital information and logo by ID. Role: ADMIN.")
    @PutMapping(value = "/info/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HospitalInfo> updateInfoAndLogo(
            @Parameter(description = "Hospital info ID") @PathVariable Long id,
            @Parameter(description = "Hospital name") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Hospital address") @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Hospital phone number") @RequestParam(value = "phone", required = false) String phone,
            @Parameter(description = "Hospital email") @RequestParam(value = "email", required = false) String email,
            @Parameter(description = "Logo file (JPG, PNG, JPEG, SVG)") @RequestPart(value = "logo", required = false) MultipartFile logo) throws Exception {
        return ResponseEntity.ok(service.updateInfoAndLogo(id, name, address, phone, email, logo));
    }

    /**
     * Deletes all hospital information.
     *
     * @return A confirmation message.
     */
    @Operation(summary = "Delete all hospital information. Role: ADMIN.")
    @DeleteMapping("/info")
    public ResponseEntity<String> deleteInfo() {
        service.deleteInfo();
        return ResponseEntity.ok("Hospital information deleted successfully");
    }
}