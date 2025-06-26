package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import java.util.List;

public interface AppointmentService {
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto);
    AppointmentResponseDTO getAppointment(Long id);
    List<AppointmentResponseDTO> listAppointments();
    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto);
    void deleteAppointment(Long id);
    boolean cancelAppointment(Long id, String initiatedBy);
} 