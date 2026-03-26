package org.example.docvanta_bcakend.service.interfaces;

import org.example.docvanta_bcakend.dto.*;
import org.example.docvanta_bcakend.entity.AppointmentStatus;

import java.time.LocalDateTime;
import java.util.List;

public interface AppointmentServiceInterface {

    List<AppointmentDTO> getAllAppointments();

    AppointmentDTO getAppointmentById(Long id);

    List<AppointmentDTO> getAppointmentsByPractitioner(Long practitionerId);

    List<AppointmentDTO> getAppointmentsByPatient(Long patientId);

    List<AppointmentDTO> getAppointmentsByStatus(AppointmentStatus status);

    List<AppointmentDTO> getAppointmentsByDateRange(LocalDateTime start, LocalDateTime end);

    PriceEstimateDTO estimatePrice(Long practitionerId, Long specialtyId);

    AppointmentDTO createAppointment(AppointmentRequest request);

    AppointmentDTO requestAppointmentBySpecialty(AppointmentBySpecialtyRequest request);

    AppointmentDTO confirmAppointment(Long id);

    AppointmentDTO rejectAppointment(Long id);

    AppointmentDTO completeAppointment(Long id);

    AppointmentDTO cancelAppointment(Long id);

    AppointmentDTO updateStatus(Long id, AppointmentStatus newStatus);

    PerformedActDTO addPerformedAct(Long appointmentId, PerformedActRequest request, Long practitionerId);

    void removePerformedAct(Long appointmentId, Long performedActId);

    List<PerformedActDTO> getPerformedActs(Long appointmentId);

    AppointmentDTO updateAppointment(Long id, AppointmentRequest request);

    void deleteAppointment(Long id);
}
