package org.example.docvanta_bcakend.entity;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@DiscriminatorValue("CLINIC_PERSONNEL")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"clinic"})
@ToString(exclude = {"clinic"})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class ClinicPersonnel extends User {

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private PersonnelType personnelType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    /**
     * Check if this personnel member is a system administrator
     */
    public boolean isSystemAdministrator() {
        return personnelType == PersonnelType.SYSTEM_ADMINISTRATOR;
    }

    /**
     * Check if this personnel member can manage appointments
     */
    public boolean canManageAppointments() {
        return personnelType != null && personnelType.canManageAppointments();
    }

    /**
     * Check if this personnel member can view patients
     */
    public boolean canViewPatients() {
        return personnelType != null && personnelType.canViewPatients();
    }
}
