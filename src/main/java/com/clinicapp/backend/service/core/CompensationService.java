package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.Appointment;

public interface CompensationService {
    void offerReschedule(Appointment appointment);
} 