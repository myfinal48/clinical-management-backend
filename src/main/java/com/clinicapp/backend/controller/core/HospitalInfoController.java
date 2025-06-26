package com.clinicapp.backend.controller.core;

import com.clinicapp.backend.model.core.HospitalInfo;
import com.clinicapp.backend.service.core.HospitalInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/hospital")
public class HospitalInfoController {
    private final HospitalInfoService service;

    public HospitalInfoController(HospitalInfoService service) {
        this.service = service;
    }

    @Operation(summary = "Get hospital information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hospital information retrieved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/info")
    public ResponseEntity<List<HospitalInfo>> getInfo() {
        return ResponseEntity.ok(service.getAllInfo());
    }

    @Operation(summary = "Save hospital information and logo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hospital information and logo saved successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or data")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/info", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HospitalInfo> saveInfoAndLogo(
            @Parameter(description = "Hospital name")
            @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Hospital address")
            @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Hospital phone number")
            @RequestParam(value = "phone", required = false) String phone,
            @Parameter(description = "Hospital email")
            @RequestParam(value = "email", required = false) String email,
            @Parameter(description = "Logo file (JPG, PNG, JPEG, SVG)")
            @RequestPart(value = "logo", required = false) MultipartFile logo) throws Exception {
        return ResponseEntity.ok(service.saveInfoAndLogo(name, address, phone, email, logo));
    }

    @Operation(summary = "Update hospital information and logo")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hospital information and logo updated successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "400", description = "Invalid file format or data"),
        @ApiResponse(responseCode = "404", description = "Hospital information not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/info/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<HospitalInfo> updateInfoAndLogo(
            @Parameter(description = "Hospital info ID")
            @PathVariable Long id,
            @Parameter(description = "Hospital name")
            @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "Hospital address")
            @RequestParam(value = "address", required = false) String address,
            @Parameter(description = "Hospital phone number")
            @RequestParam(value = "phone", required = false) String phone,
            @Parameter(description = "Hospital email")
            @RequestParam(value = "email", required = false) String email,
            @Parameter(description = "Logo file (JPG, PNG, JPEG, SVG)")
            @RequestPart(value = "logo", required = false) MultipartFile logo) throws Exception {
        return ResponseEntity.ok(service.updateInfoAndLogo(id, name, address, phone, email, logo));
    }

    @Operation(summary = "Delete hospital information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Hospital information deleted successfully"),
        @ApiResponse(responseCode = "403", description = "Access denied"),
        @ApiResponse(responseCode = "404", description = "Hospital information not found")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/info")
    public ResponseEntity<String> deleteInfo() {
        service.deleteInfo();
        return ResponseEntity.ok("Hospital information deleted successfully");
    }
} 