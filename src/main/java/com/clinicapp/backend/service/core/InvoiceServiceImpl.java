package com.clinicapp.backend.service.core;

import com.clinicapp.backend.dto.core.InvoiceRequestDTO;
import com.clinicapp.backend.dto.core.InvoiceResponseDTO;
import com.clinicapp.backend.exceptions.ResourceNotFoundException;
import com.clinicapp.backend.model.core.Invoice;
import com.clinicapp.backend.model.core.Patient;
import com.clinicapp.backend.repository.core.InvoiceRepository;
import com.clinicapp.backend.repository.core.PatientRepository;
import com.clinicapp.backend.mapper.InvoiceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import com.clinicapp.backend.service.notification.NotificationService;
import com.clinicapp.backend.dto.notification.NotificationRequestDTO;
import com.clinicapp.backend.model.notification.NotificationType;
import com.clinicapp.backend.model.notification.NotificationChannel;
import java.util.Set;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class InvoiceServiceImpl implements InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final PatientRepository patientRepository;
    private final InvoiceMapper invoiceMapper;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public InvoiceResponseDTO createInvoice(InvoiceRequestDTO dto) {
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));
        Invoice invoice = invoiceMapper.toEntity(dto, patient);
        InvoiceResponseDTO response = invoiceMapper.toResponseDTO(invoiceRepository.save(invoice));
        // Suppression de l'envoi de notification au patient
        return response;
    }

    @Override
    @Transactional
    public InvoiceResponseDTO updateInvoice(Long id, InvoiceRequestDTO dto) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        Patient patient = patientRepository.findById(dto.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + dto.getPatientId()));
        invoiceMapper.updateEntity(invoice, dto, patient);
        return invoiceMapper.toResponseDTO(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        if (!invoiceRepository.existsById(id)) {
            throw new ResourceNotFoundException("Invoice not found with id: " + id);
        }
        invoiceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponseDTO getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        return invoiceMapper.toResponseDTO(invoice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getAllInvoices() {
        return invoiceRepository.findAll().stream().map(invoiceMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getInvoicesByPatient(Long patientId) {
        return invoiceRepository.findByPatientId(patientId).stream().map(invoiceMapper::toResponseDTO).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public InvoiceResponseDTO markAsPaid(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
        invoice.setPaid(true);
        invoice.setDatePaid(LocalDateTime.now());
        return invoiceMapper.toResponseDTO(invoiceRepository.save(invoice));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getPaidInvoices() {
        return invoiceRepository.findAll().stream()
                .filter(Invoice::isPaid)
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<InvoiceResponseDTO> getUnpaidInvoices() {
        return invoiceRepository.findAll().stream()
                .filter(invoice -> !invoice.isPaid())
                .map(invoiceMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getTotalPaidAmount() {
        return invoiceRepository.findAll().stream()
                .filter(Invoice::isPaid)
                .map(Invoice::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    @Transactional(readOnly = true)
    public Invoice getInvoiceEntityById(Long id) {
        return invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found with id: " + id));
    }
} 