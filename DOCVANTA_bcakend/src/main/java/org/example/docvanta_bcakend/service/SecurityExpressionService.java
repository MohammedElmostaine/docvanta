package org.example.docvanta_bcakend.service;

import lombok.RequiredArgsConstructor;
import org.example.docvanta_bcakend.entity.User;
import org.example.docvanta_bcakend.repository.AppointmentRepository;
import org.example.docvanta_bcakend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service providing security expression methods for use in @PreAuthorize annotations.
 * These methods enable resource-level authorization checks.
 *
 * Usage example: @PreAuthorize("@securityService.canAccessPatient(#id, authentication)")
 */
@Service("securityService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SecurityExpressionService {

    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;

    /**
     * Check if the authenticated user is accessing their own resource.
     */
    public boolean isCurrentUser(Long userId, Authentication auth) {
        Long currentUserId = getCurrentUserId(auth);
        return currentUserId != null && currentUserId.equals(userId);
    }

    /**
     * Check if a practitioner has the patient assigned (via appointments).
     */
    public boolean isAssignedPatient(Long patientId, Authentication auth) {
        Long practitionerId = getCurrentUserId(auth);
        if (practitionerId == null) return false;
        return appointmentRepository.existsByPractitionerUserIdAndPatientUserId(practitionerId, patientId);
    }

    /**
     * Check if the current user can access a patient's data.
     * Rules:
     * - SYSTEM_ADMINISTRATOR: Can access all patients
     * - PRACTITIONER: Can access patients they have appointments with
     * - RECEPTIONIST: Can access all patients (front-desk role)
     * - PATIENT: Can only access their own data
     */
    public boolean canAccessPatient(Long patientId, Authentication auth) {
        String role = getRole(auth);
        Long userId = getCurrentUserId(auth);

        if (userId == null) return false;

        return switch (role) {
            case "SYSTEM_ADMINISTRATOR", "RECEPTIONIST" -> true;
            case "PRACTITIONER" -> appointmentRepository.existsByPractitionerUserIdAndPatientUserId(userId, patientId);
            case "PATIENT" -> userId.equals(patientId);
            default -> false;
        };
    }

    /**
     * Check if the current user can access an appointment.
     * Rules:
     * - SYSTEM_ADMINISTRATOR: Can access all appointments
     * - PRACTITIONER: Can access their own appointments
     * - RECEPTIONIST: Can access all appointments (front-desk role)
     * - PATIENT: Can only access their own appointments
     */
    public boolean canAccessAppointment(Long appointmentPractitionerId, Long appointmentPatientId, Authentication auth) {
        String role = getRole(auth);
        Long userId = getCurrentUserId(auth);

        if (userId == null) return false;

        return switch (role) {
            case "SYSTEM_ADMINISTRATOR", "RECEPTIONIST" -> true;
            case "PRACTITIONER" -> userId.equals(appointmentPractitionerId);
            case "PATIENT" -> userId.equals(appointmentPatientId);
            default -> false;
        };
    }

    /**
     * Check if the current user can access a document.
     * Rules:
     * - SYSTEM_ADMINISTRATOR: Can access all documents
     * - PRACTITIONER: Can access documents they created or for their patients
     * - RECEPTIONIST: Can access all documents (read-only, front-desk role)
     * - PATIENT: Can only access their own authorized documents
     */
    public boolean canAccessDocument(Long documentPractitionerId, Long documentPatientId,
                                     Boolean authorizedForPatient, Authentication auth) {
        String role = getRole(auth);
        Long userId = getCurrentUserId(auth);

        if (userId == null) return false;

        return switch (role) {
            case "SYSTEM_ADMINISTRATOR", "RECEPTIONIST" -> true;
            case "PRACTITIONER" -> userId.equals(documentPractitionerId) ||
                            appointmentRepository.existsByPractitionerUserIdAndPatientUserId(userId, documentPatientId);
            case "PATIENT" -> userId.equals(documentPatientId) && Boolean.TRUE.equals(authorizedForPatient);
            default -> false;
        };
    }

    /**
     * Check if the current user can create/modify medical documents.
     * Only SYSTEM_ADMINISTRATOR and PRACTITIONER can create medical documents.
     */
    public boolean canCreateDocument(Authentication auth) {
        String role = getRole(auth);
        return "SYSTEM_ADMINISTRATOR".equals(role) || "PRACTITIONER".equals(role);
    }

    // ========== Helper Methods ==========

    private Long getCurrentUserId(Authentication auth) {
        if (auth == null || auth.getName() == null) {
            return null;
        }
        try {
            return userRepository.findByUsername(auth.getName())
                    .map(User::getUserId)
                    .orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    public Long getCurrentUserId(Authentication auth, Long fallbackUserId) {
        Long userId = getCurrentUserId(auth);
        return userId != null ? userId : fallbackUserId;
    }

    private String getRole(Authentication auth) {
        if (auth == null || auth.getAuthorities() == null) {
            return null;
        }

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(a -> a.startsWith("ROLE_"))
                .findFirst()
                .map(r -> r.substring(5))
                .orElse(null);
    }

    public boolean hasRole(Authentication auth, String role) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    public boolean hasPermission(Authentication auth, String permission) {
        if (auth == null || auth.getAuthorities() == null) {
            return false;
        }
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(permission));
    }
}
