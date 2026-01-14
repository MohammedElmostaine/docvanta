package org.example.docvanta_bcakend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "clinics")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long clinicId;

    @Column(nullable = false)
    private String name;

    private String address;

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Practitioner> practitioners = new ArrayList<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ClinicPersonnel> personnelMembers = new ArrayList<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Patient> patients = new ArrayList<>();

    @OneToMany(mappedBy = "clinic", cascade = CascadeType.ALL)
    @Builder.Default
    private List<ClinicalDepartment> departments = new ArrayList<>();
}
