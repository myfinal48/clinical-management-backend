package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.AppointmentRequestDTO;
import com.clinicapp.backend.dto.core.AppointmentResponseDTO;
import com.clinicapp.backend.dto.core.TimeSlotConflictDTO;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AppointmentService {
    AppointmentResponseDTO createAppointment(AppointmentRequestDTO dto);
    AppointmentResponseDTO getAppointment(Long id);
    AppointmentResponseDTO updateAppointment(Long id, AppointmentRequestDTO dto);
    void deleteAppointment(Long id);
    boolean cancelAppointment(Long id, String initiatedBy);
    List<java.time.OffsetDateTime> findAlternativeSlots(String doctor, java.time.OffsetDateTime desiredTime);
    Page<AppointmentResponseDTO> listAppointments(Pageable pageable);
    Page<AppointmentResponseDTO> listAppointmentsFiltered(String doctor, String date, String room, String status, Pageable pageable);
    AppointmentResponseDTO markAsCompleted(Long id);
    TimeSlotConflictDTO checkTimeSlotConflict(String doctor, java.time.OffsetDateTime desiredTime, Long excludeId);
}