package org.example.docvanta_bcakend.service;

import org.example.docvanta_bcakend.dto.*;
import org.example.docvanta_bcakend.entity.*;
import org.example.docvanta_bcakend.exception.ResourceNotFoundException;
import org.example.docvanta_bcakend.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;
    private final PractitionerRepository practitionerRepository;
    private final PatientRepository patientRepository;
    private final SpecialtyRepository specialtyRepository;
    private final MedicalActRepository medicalActRepository;
    private final PerformedActRepository performedActRepository;
    private final InvoiceRepository invoiceRepository;
    private final SchedulingService schedulingService;

    public AppointmentService(AppointmentRepository appointmentRepository,
            PractitionerRepository practitionerRepository,
            PatientRepository patientRepository,
            SpecialtyRepository specialtyRepository,
            MedicalActRepository medicalActRepository,
            PerformedActRepository performedActRepository,
            InvoiceRepository invoiceRepository,
            SchedulingService schedulingService) {
        this.appointmentRepository = appointmentRepository;
        this.practitionerRepository = practitionerRepository;
        this.patientRepository = patientRepository;
        this.specialtyRepository = specialtyRepository;
        this.medicalActRepository = medicalActRepository;
        this.performedActRepository = performedActRepository;
        this.invoiceRepository = invoiceRepository;
        this.schedulingService = schedulingService;
    }

    // ═══════════════════════════════════════════════════════════
    //  QUERIES
    // ═══════════════════════════════════════════════════════════

    public List<AppointmentDTO> getAllAppointments() {
        return appointmentRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public AppointmentDTO getAppointmentById(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        return toDetailDTO(appointment);
    }

    public List<AppointmentDTO> getAppointmentsByPractitioner(Long practitionerId) {
        return appointmentRepository.findByPractitionerUserId(practitionerId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByPatient(Long patientId) {
        return appointmentRepository.findByPatientUserId(patientId).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status) {
        return appointmentRepository.findByStatus(status).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    public List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end) {
        return appointmentRepository.findByDatetimeBetween(start, end).stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  PRICE ESTIMATION (shown before booking)
    // ═══════════════════════════════════════════════════════════

    public PriceEstimateDTO estimatePrice(Long practitionerId, Long specialtyId) {
        Practitioner practitioner = practitionerRepository.findById(practitionerId)
                .orElseThrow(() -> new ResourceNotFoundException("Practitioner not found"));

        BigDecimal basePrice = findConsultationPrice(practitioner);
        List<PriceEstimateDTO.EstimatedActDTO> estimatedActs = new ArrayList<>();
        String specialtyName = "";

        if (specialtyId != null) {
            specialtyName = specialtyRepository.findById(specialtyId)
                    .map(Specialty::getName).orElse("");

            if (practitioner.getDepartment() != null) {
                List<MedicalAct> deptActs = medicalActRepository.findAll().stream()
                        .filter(a -> a.getActive() && a.getDepartment() != null
                                && a.getDepartment().getDepartmentId().equals(practitioner.getDepartment().getDepartmentId())
                                && a.getCategory() != MedicalActCategory.CONSULTATION)
                        .limit(5)
                        .collect(Collectors.toList());

                for (MedicalAct act : deptActs) {
                    estimatedActs.add(PriceEstimateDTO.EstimatedActDTO.builder()
                            .actCode(act.getCode())
                            .actName(act.getName())
                            .category(act.getCategory().name())
                            .price(act.getBasePrice())
                            .build());
                }
            }
        }

        return PriceEstimateDTO.builder()
                .baseConsultationPrice(basePrice)
                .estimatedActs(estimatedActs)
                .totalEstimatedPrice(basePrice)
                .practitionerName(practitioner.getFirstName() + " " + practitioner.getLastName())
                .specialtyName(specialtyName)
                .build();
    }

    // ═══════════════════════════════════════════════════════════
    //  APPOINTMENT CREATION (with slot validation)
    // ═══════════════════════════════════════════════════════════

    /**
     * Create an appointment with direct practitioner assignment.
     *
     * Business rules enforced:
     * 1. Default status = PENDING (always)
     * 2. Default paymentStatus = UNPAID
     * 3. Time must be 08:00–18:00
     * 4. Slot must be free (no double booking)
     * 5. Estimated price auto-calculated
     */
    public AppointmentDTO createAppointment(AppointmentRequest request) {
        Practitioner practitioner = practitionerRepository.findById(request.getPractitionerId())
                .orElseThrow(() -> new ResourceNotFoundException("Practitioner not found with id: " + request.getPractitionerId()));

        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        // Validate slot availability (business hours + no conflict)
        schedulingService.validateSlotAvailability(request.getPractitionerId(), request.getDatetime());

        BigDecimal estimatedPrice = findConsultationPrice(practitioner);

        Appointment appointment = Appointment.builder()
                .referenceNumber(generateReferenceNumber())
                .datetime(request.getDatetime())
                .status(AppointmentStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .practitioner(practitioner)
                .patient(patient)
                .reason(request.getReason())
                .estimatedPrice(estimatedPrice)
                .build();

        return toDTO(appointmentRepository.save(appointment));
    }

    /**
     * Request appointment by specialty — system assigns best practitioner.
     * Same validation rules as createAppointment.
     */
    public AppointmentDTO requestAppointmentBySpecialty(AppointmentBySpecialtyRequest request) {
        Patient patient = patientRepository.findById(request.getPatientId())
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));

        Specialty specialty = specialtyRepository.findById(request.getSpecialtyId())
                .orElseThrow(() -> new ResourceNotFoundException("Specialty not found with id: " + request.getSpecialtyId()));

        Practitioner practitioner;
        if (request.getPractitionerId() != null) {
            practitioner = practitionerRepository.findById(request.getPractitionerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Practitioner not found with id: " + request.getPractitionerId()));

            boolean hasSpecialty = practitioner.getSpecialties().stream()
                    .anyMatch(s -> s.getSpecialtyId().equals(request.getSpecialtyId()));
            if (!hasSpecialty) {
                throw new IllegalArgumentException("Selected practitioner does not have the requested specialty");
            }
        } else {
            practitioner = findAvailablePractitionerForSpecialty(request.getSpecialtyId(), request.getPreferredDatetime());
            if (practitioner == null) {
                throw new ResourceNotFoundException("No available practitioner found for the requested specialty and time");
            }
        }

        // Validate slot availability
        schedulingService.validateSlotAvailability(practitioner.getUserId(), request.getPreferredDatetime());

        BigDecimal estimatedPrice = findConsultationPrice(practitioner);

        Appointment appointment = Appointment.builder()
                .referenceNumber(generateReferenceNumber())
                .datetime(request.getPreferredDatetime())
                .status(AppointmentStatus.PENDING)
                .paymentStatus(PaymentStatus.UNPAID)
                .practitioner(practitioner)
                .patient(patient)
                .requestedSpecialty(specialty)
                .reason(request.getReason())
                .estimatedPrice(estimatedPrice)
                .build();

        return toDTO(appointmentRepository.save(appointment));
    }

    // ═══════════════════════════════════════════════════════════
    //  STATUS TRANSITIONS
    //  PENDING → CONFIRMED → COMPLETED → INVOICED → PAID
    // ═══════════════════════════════════════════════════════════

    /**
     * Staff confirms a pending appointment.
     */
    public AppointmentDTO confirmAppointment(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be confirmed. Current: " + appointment.getStatus());
        }
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        return toDTO(appointmentRepository.save(appointment));
    }

    /**
     * Staff rejects a pending appointment.
     */
    public AppointmentDTO rejectAppointment(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            throw new IllegalStateException("Only pending appointments can be rejected");
        }
        appointment.setStatus(AppointmentStatus.REJECTED);
        return toDTO(appointmentRepository.save(appointment));
    }

    /**
     * Doctor completes the consultation. Recalculates final price from performed acts.
     */
    public AppointmentDTO completeAppointment(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed appointments can be completed. Current: " + appointment.getStatus());
        }
        List<PerformedAct> acts = performedActRepository.findByAppointmentAppointmentId(id);
        if (acts.isEmpty()) {
            throw new IllegalStateException("Cannot complete appointment without performed acts. Please add at least one act first.");
        }
        appointment.recalculateFinalPrice();
        appointment.setStatus(AppointmentStatus.COMPLETED);
        return toDetailDTO(appointmentRepository.save(appointment));
    }

    /**
     * Cancel an appointment (from any non-terminal state).
     */
    public AppointmentDTO cancelAppointment(Long id) {
        Appointment appointment = findAppointmentOrThrow(id);
        if (appointment.getStatus().isTerminal()) {
            throw new IllegalStateException("Appointment is already in terminal status: " + appointment.getStatus());
        }
        appointment.setStatus(AppointmentStatus.CANCELLED);
        return toDTO(appointmentRepository.save(appointment));
    }

    /**
     * Generic status update — validates allowed transitions.
     */
    public AppointmentDTO updateStatus(Long id, AppointmentStatus newStatus) {
        Appointment appointment = findAppointmentOrThrow(id);
        AppointmentStatus current = appointment.getStatus();

        // Validate transition
        boolean valid = switch (newStatus) {
            case CONFIRMED -> current == AppointmentStatus.PENDING;
            case COMPLETED -> current == AppointmentStatus.CONFIRMED;
            case INVOICED  -> current == AppointmentStatus.COMPLETED;
            case PAID      -> current == AppointmentStatus.INVOICED;
            case REJECTED  -> current == AppointmentStatus.PENDING;
            case CANCELLED -> !current.isTerminal();
            case PENDING   -> false; // Cannot go back to PENDING
        };

        if (!valid) {
            throw new IllegalStateException("Cannot transition from " + current + " to " + newStatus);
        }

        appointment.setStatus(newStatus);
        return toDTO(appointmentRepository.save(appointment));
    }

    // ═══════════════════════════════════════════════════════════
    //  PERFORMED ACTS (doctor adds during consultation)
    // ═══════════════════════════════════════════════════════════

    public PerformedActDTO addPerformedAct(Long appointmentId, PerformedActRequest request, Long practitionerId) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (!appointment.getStatus().canAddPerformedActs()) {
            throw new IllegalStateException(
                    "Cannot add acts in status " + appointment.getStatus() + ". Appointment must be CONFIRMED or COMPLETED.");
        }

        MedicalAct medicalAct = medicalActRepository.findById(request.getMedicalActId())
                .orElseThrow(() -> new ResourceNotFoundException("Medical act not found"));

        if (!medicalAct.getActive()) {
            throw new IllegalStateException("Cannot add an inactive medical act");
        }

        Practitioner performedBy = null;
        if (practitionerId != null) {
            performedBy = practitionerRepository.findById(practitionerId).orElse(null);
        }

        int quantity = request.getQuantity() != null ? request.getQuantity() : 1;

        PerformedAct performedAct = PerformedAct.builder()
                .appointment(appointment)
                .medicalAct(medicalAct)
                .performedBy(performedBy)
                .quantity(quantity)
                .unitPrice(medicalAct.getBasePrice())
                .totalPrice(medicalAct.getBasePrice().multiply(BigDecimal.valueOf(quantity)))
                .notes(request.getNotes())
                .performedAt(LocalDateTime.now())
                .build();

        performedAct = performedActRepository.save(performedAct);

        appointment.getPerformedActs().add(performedAct);
        appointment.recalculateFinalPrice();
        appointmentRepository.save(appointment);

        return toPerformedActDTO(performedAct);
    }

    public void removePerformedAct(Long appointmentId, Long performedActId) {
        Appointment appointment = findAppointmentOrThrow(appointmentId);

        if (appointment.getStatus() == AppointmentStatus.INVOICED || appointment.getStatus() == AppointmentStatus.PAID) {
            throw new IllegalStateException("Cannot remove acts after invoice has been generated");
        }

        PerformedAct act = performedActRepository.findById(performedActId)
                .orElseThrow(() -> new ResourceNotFoundException("Performed act not found"));

        if (!act.getAppointment().getAppointmentId().equals(appointmentId)) {
            throw new IllegalArgumentException("Performed act does not belong to this appointment");
        }

        appointment.getPerformedActs().remove(act);
        performedActRepository.delete(act);
        appointment.recalculateFinalPrice();
        appointmentRepository.save(appointment);
    }

    public List<PerformedActDTO> getPerformedActs(Long appointmentId) {
        return performedActRepository.findByAppointmentAppointmentId(appointmentId).stream()
                .map(this::toPerformedActDTO)
                .collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════
    //  UPDATE & DELETE
    // ═══════════════════════════════════════════════════════════

    public AppointmentDTO updateAppointment(Long id, AppointmentRequest request) {
        Appointment appointment = findAppointmentOrThrow(id);

        if (!appointment.isModifiable()) {
            throw new IllegalStateException("Appointment cannot be modified in status: " + appointment.getStatus());
        }

        if (request.getDatetime() != null && !request.getDatetime().equals(appointment.getDatetime())) {
            // Re-validate slot if datetime is changing
            schedulingService.validateSlotAvailability(
                    appointment.getPractitioner().getUserId(), request.getDatetime());
            appointment.setDatetime(request.getDatetime());
        }
        if (request.getPractitionerId() != null && !request.getPractitionerId().equals(appointment.getPractitioner().getUserId())) {
            Practitioner practitioner = practitionerRepository.findById(request.getPractitionerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Practitioner not found with id: " + request.getPractitionerId()));
            // Validate new practitioner's slot
            schedulingService.validateSlotAvailability(request.getPractitionerId(), appointment.getDatetime());
            appointment.setPractitioner(practitioner);
        }
        if (request.getPatientId() != null) {
            Patient patient = patientRepository.findById(request.getPatientId())
                    .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + request.getPatientId()));
            appointment.setPatient(patient);
        }
        if (request.getReason() != null) {
            appointment.setReason(request.getReason());
        }

        return toDTO(appointmentRepository.save(appointment));
    }

    public void deleteAppointment(Long id) {
        if (!appointmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Appointment not found with id: " + id);
        }
        appointmentRepository.deleteById(id);
    }

    // ═══════════════════════════════════════════════════════════
    //  PRIVATE HELPERS
    // ═══════════════════════════════════════════════════════════

    private String generateReferenceNumber() {
        long count = appointmentRepository.count();
        return "APT-" + String.format("%05d", count + 1);
    }

    private Appointment findAppointmentOrThrow(Long id) {
        return appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));
    }

    /**
     * Find the base consultation price for a practitioner's clinic.
     */
    private BigDecimal findConsultationPrice(Practitioner practitioner) {
        if (practitioner.getClinic() != null) {
            List<MedicalAct> clinicActs = medicalActRepository
                    .findByClinicClinicIdAndActiveTrue(practitioner.getClinic().getClinicId());

            for (MedicalAct act : clinicActs) {
                if (act.getCategory() == MedicalActCategory.CONSULTATION) {
                    return act.getBasePrice();
                }
            }
        }

        // Fallback: any consultation act
        List<MedicalAct> consultationActs = medicalActRepository.findByCategory(MedicalActCategory.CONSULTATION);
        if (!consultationActs.isEmpty()) {
            return consultationActs.get(0).getBasePrice();
        }

        return BigDecimal.ZERO;
    }

    private Practitioner findAvailablePractitionerForSpecialty(Long specialtyId, LocalDateTime preferredTime) {
        List<Practitioner> practitioners = practitionerRepository.findBySpecialtyId(specialtyId);

        Practitioner best = null;
        long minAppointments = Long.MAX_VALUE;

        for (Practitioner p : practitioners) {
            if (!p.getEnabled()) continue;

            long count = appointmentRepository.countByPractitionerUserIdAndStatus(
                    p.getUserId(), AppointmentStatus.CONFIRMED);

            if (count < minAppointments) {
                minAppointments = count;
                best = p;
            }
        }

        return best;
    }

    // ═══════════════════════════════════════════════════════════
    //  DTO CONVERSION
    // ═══════════════════════════════════════════════════════════

    private AppointmentDTO toDTO(Appointment appointment) {
        AppointmentDTO.AppointmentDTOBuilder builder = AppointmentDTO.builder()
                .appointmentId(appointment.getAppointmentId())
                .referenceNumber(appointment.getReferenceNumber())
                .datetime(appointment.getDatetime())
                .status(appointment.getStatus().name())
                .paymentStatus(appointment.getPaymentStatus().name())
                .practitionerId(appointment.getPractitioner().getUserId())
                .practitionerName(appointment.getPractitioner().getFirstName() + " " + appointment.getPractitioner().getLastName())
                .patientId(appointment.getPatient().getUserId())
                .patientName(appointment.getPatient().getFirstName() + " " + appointment.getPatient().getLastName())
                .reason(appointment.getReason())
                .estimatedPrice(appointment.getEstimatedPrice())
                .finalPrice(appointment.getFinalPrice());

        if (appointment.getRequestedSpecialty() != null) {
            builder.specialtyId(appointment.getRequestedSpecialty().getSpecialtyId())
                   .specialtyName(appointment.getRequestedSpecialty().getName());
        }

        // Include invoice info if available
        invoiceRepository.findByAppointmentAppointmentId(appointment.getAppointmentId())
                .ifPresent(invoice -> {
                    builder.invoiceId(invoice.getInvoiceId());
                    builder.invoiceStatus(invoice.getStatus().name());
                });

        return builder.build();
    }

    private AppointmentDTO toDetailDTO(Appointment appointment) {
        AppointmentDTO dto = toDTO(appointment);

        List<PerformedActDTO> acts = performedActRepository.findByAppointmentAppointmentId(appointment.getAppointmentId())
                .stream()
                .map(this::toPerformedActDTO)
                .collect(Collectors.toList());
        dto.setPerformedActs(acts);

        return dto;
    }

    private PerformedActDTO toPerformedActDTO(PerformedAct act) {
        return PerformedActDTO.builder()
                .performedActId(act.getPerformedActId())
                .appointmentId(act.getAppointment().getAppointmentId())
                .medicalActId(act.getMedicalAct().getMedicalActId())
                .medicalActCode(act.getMedicalAct().getCode())
                .medicalActName(act.getMedicalAct().getName())
                .category(act.getMedicalAct().getCategory().name())
                .performedById(act.getPerformedBy() != null ? act.getPerformedBy().getUserId() : null)
                .performedByName(act.getPerformedBy() != null
                        ? act.getPerformedBy().getFirstName() + " " + act.getPerformedBy().getLastName() : null)
                .quantity(act.getQuantity())
                .unitPrice(act.getUnitPrice())
                .totalPrice(act.getTotalPrice())
                .notes(act.getNotes())
                .performedAt(act.getPerformedAt())
                .build();
    }
}
