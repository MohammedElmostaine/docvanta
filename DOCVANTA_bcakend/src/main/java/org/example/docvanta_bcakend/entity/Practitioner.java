package org.example.docvanta_bcakend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

@Entity
@DiscriminatorValue("PRACTITIONER")
@Data
@EqualsAndHashCode(callSuper = true, exclude = {"specialties", "schedules", "department", "clinic"})
@ToString(exclude = {"specialties", "schedules", "department", "clinic"})
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Practitioner extends User {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "practitioner_specialties", joinColumns = @JoinColumn(name = "practitioner_id"), inverseJoinColumns = @JoinColumn(name = "specialty_id"))
    @Builder.Default
    private List<Specialty> specialties = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "practitioner_id")
    @Builder.Default
    private List<PractitionerSchedule> schedules = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private ClinicalDepartment department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

}
