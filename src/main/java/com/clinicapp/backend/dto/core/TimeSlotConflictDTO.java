package com.clinicapp.backend.dto.core;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TimeSlotConflictDTO {
    private boolean hasConflict;
    private String message;
    private List<AppointmentResponseDTO> conflictingAppointments;
}
