package com.clinicapp.backend;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.InvoiceRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.mapper.InvoiceMapper;
import com.clinicapp.backend.service.core.InvoiceServiceImpl;
import com.clinicapp.backend.service.notification.NotificationService;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.*;

class InvoiceServiceImplTest {
    @Test
    void testNoNotificationSentOnInvoiceCreation() {
        // Mocks
        InvoiceRepository invoiceRepository = mock(InvoiceRepository.class);
        PatientRepository patientRepository = mock(PatientRepository.class);
        InvoiceMapper invoiceMapper = mock(InvoiceMapper.class);
        NotificationService notificationService = mock(NotificationService.class);

        // Service
        InvoiceServiceImpl service = new InvoiceServiceImpl(
            invoiceRepository, patientRepository, invoiceMapper, notificationService
        );

        // Data
        Long patientId = 2L;
        InvoiceRequestDTO dto = new InvoiceRequestDTO();
        dto.setPatientId(patientId);
        // ... set other fields as needed

        Patient patient = new Patient();
        patient.setId(patientId);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        com.clinicapp.backend.model.core.Invoice invoice = new com.clinicapp.backend.model.core.Invoice();
        invoice.setId(123L);
        invoice.setPatient(patient);
        when(invoiceRepository.save(any(com.clinicapp.backend.model.core.Invoice.class))).thenReturn(invoice);
        when(invoiceMapper.toEntity(any(), any())).thenReturn(invoice);
        when(invoiceMapper.toResponseDTO(any())).thenReturn(mock(com.clinicapp.backend.dto.core.InvoiceResponseDTO.class));

        // Appel
        service.createInvoice(dto);

        // Vérification : aucune notification envoyée
        verify(notificationService, never()).sendNotification(any());
    }
} 