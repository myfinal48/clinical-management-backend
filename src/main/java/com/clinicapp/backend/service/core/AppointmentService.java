package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto);
    AppointmentResponseDTO getAppointment(Long id);
    List<AppointmentResponseDTO> listAppointments();
    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto);
    void deleteAppointment(Long id);
    boolean cancelAppointment(Long id, String initiatedBy);
    List<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room);
    List<java.time.OffsetDateTime> findAlternativeSlots(String doctor, java.time.OffsetDateTime desiredTime);
    Page<AppointmentResponseDTO> listAppointments(Pageable pageable);
    Page<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room, Pageable pageable);
    AppointmentResponseDTO markAsCompleted(Long id);
} 