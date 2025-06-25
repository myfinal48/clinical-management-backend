package com.clinicapp.backend.service.core;

import com.clinicapp.backend.model.core.Appointment;

public interface NotificationService {
    void sendCancellation(Appointment appointment, String initiatedBy);
} 