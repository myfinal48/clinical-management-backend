package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentDTO;
import java.util.List;

public interface AppointmentService {
    AppointmentDTO createAppointment(AppointmentDTO dto);
    AppointmentDTO getAppointment(Long id);
    List<AppointmentDTO> listAppointments();
    AppointmentDTO updateAppointment(Long id, AppointmentDTO dto);
    void deleteAppointment(Long id);
    boolean cancelAppointment(Long id, String initiatedBy);
} 