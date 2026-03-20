package org.example.docvanta_bcakend.config;

import org.example.docvanta_bcakend.entity.*;
import org.example.docvanta_bcakend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(
            UserRoleRepository userRoleRepository,
            AccessRightRepository accessRightRepository,
            UserRepository userRepository,
            ClinicRepository clinicRepository,
            ClinicalDepartmentRepository departmentRepository,
            SpecialtyRepository specialtyRepository,
            AppointmentRepository appointmentRepository,
            MedicalDocumentRepository documentRepository,
            PatientRecordRepository patientRecordRepository,
            MedicalActRepository medicalActRepository,
            InvoiceRepository invoiceRepository,
            InvoiceLineRepository invoiceLineRepository,
            PaymentRepository paymentRepository,
            PerformedActRepository performedActRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // ==================== 1. ROLES & ACCESS RIGHTS ====================
            createRoleIfNotExists(userRoleRepository, accessRightRepository, "SYSTEM_ADMINISTRATOR", List.of(
                    accessRight("MANAGE_USERS", "Create, update, delete all users"),
                    accessRight("MANAGE_PRACTITIONERS", "Full access to practitioner management"),
                    accessRight("MANAGE_PATIENTS", "Full access to patient management"),
                    accessRight("MANAGE_PERSONNEL", "Full access to clinic personnel management"),
                    accessRight("MANAGE_APPOINTMENTS", "Full access to appointment management"),
                    accessRight("MANAGE_DOCUMENTS", "Full access to document management"),
                    accessRight("MANAGE_CLINICS", "Full access to clinic management"),
                    accessRight("MANAGE_DEPARTMENTS", "Full access to department management"),
                    accessRight("MANAGE_PATIENT_RECORDS", "Full access to patient records"),
                    accessRight("VIEW_ALL", "View all resources")
            ));

            createRoleIfNotExists(userRoleRepository, accessRightRepository, "PRACTITIONER", List.of(
                    accessRight("VIEW_OWN_APPOINTMENTS", "View own appointments"),
                    accessRight("MANAGE_OWN_APPOINTMENTS", "Update own appointment status"),
                    accessRight("VIEW_PATIENTS", "View patient information"),
                    accessRight("CREATE_MEDICAL_DOCUMENTS", "Create medical documents"),
                    accessRight("VIEW_MEDICAL_DOCUMENTS", "View medical documents"),
                    accessRight("VIEW_PATIENT_RECORDS", "View patient records"),
                    accessRight("UPDATE_PATIENT_RECORDS", "Update patient records")
            ));

            createRoleIfNotExists(userRoleRepository, accessRightRepository, "PATIENT", List.of(
                    accessRight("VIEW_OWN_APPOINTMENTS", "View own appointments"),
                    accessRight("REQUEST_APPOINTMENT", "Request new appointment"),
                    accessRight("CANCEL_OWN_APPOINTMENT", "Cancel own pending appointment"),
                    accessRight("VIEW_OWN_MEDICAL_DOCUMENTS", "View own authorized documents"),
                    accessRight("VIEW_OWN_PATIENT_RECORD", "View own patient record")
            ));

            createRoleIfNotExists(userRoleRepository, accessRightRepository, "RECEPTIONIST", List.of(
                    accessRight("VIEW_ALL_APPOINTMENTS", "View all appointments across departments"),
                    accessRight("MANAGE_ALL_APPOINTMENTS", "Manage appointments for all practitioners"),
                    accessRight("VIEW_ALL_PATIENTS", "View all patients"),
                    accessRight("REGISTER_PATIENTS", "Register and update patients"),
                    accessRight("MANAGE_INVOICES", "Create and manage invoices"),
                    accessRight("MANAGE_PAYMENTS", "Record and manage payments"),
                    accessRight("MANAGE_MEDICAL_ACTS", "Manage medical acts catalog"),
                    accessRight("CHECKIN_PATIENTS", "Check-in patients for appointments")
            ));

            // Skip user seeding if users already exist
            if (userRepository.count() > 0) {
                return;
            }

            // ==================== 2. FETCH ROLES ====================
            UserRole adminRole = userRoleRepository.findByName("SYSTEM_ADMINISTRATOR").orElseThrow();
            UserRole practitionerRole = userRoleRepository.findByName("PRACTITIONER").orElseThrow();
            UserRole patientRole = userRoleRepository.findByName("PATIENT").orElseThrow();
            UserRole receptionistRole = userRoleRepository.findByName("RECEPTIONIST").orElseThrow();

            String encodedPassword = passwordEncoder.encode("password123");

            // ==================== 3. CLINIC & DEPARTMENTS ====================
            Clinic clinic = clinicRepository.save(Clinic.builder()
                    .name("DocVanta Medical Center")
                    .address("123 Health Avenue, Casablanca, Morocco")
                    .build());

            ClinicalDepartment cardiology = departmentRepository.save(ClinicalDepartment.builder()
                    .name("Cardiology").clinic(clinic).build());
            ClinicalDepartment neurology = departmentRepository.save(ClinicalDepartment.builder()
                    .name("Neurology").clinic(clinic).build());
            ClinicalDepartment generalMedicine = departmentRepository.save(ClinicalDepartment.builder()
                    .name("General Medicine").clinic(clinic).build());
            ClinicalDepartment pediatrics = departmentRepository.save(ClinicalDepartment.builder()
                    .name("Pediatrics").clinic(clinic).build());
            ClinicalDepartment dermatology = departmentRepository.save(ClinicalDepartment.builder()
                    .name("Dermatology").clinic(clinic).build());

            // ==================== 4. SPECIALTIES ====================
            Specialty cardioSpec = getOrCreateSpecialty(specialtyRepository, "Cardiology");
            Specialty neuroSpec = getOrCreateSpecialty(specialtyRepository, "Neurology");
            Specialty generalSpec = getOrCreateSpecialty(specialtyRepository, "General Medicine");
            Specialty pediatricsSpec = getOrCreateSpecialty(specialtyRepository, "Pediatrics");
            Specialty dermaSpec = getOrCreateSpecialty(specialtyRepository, "Dermatology");
            Specialty surgerySpec = getOrCreateSpecialty(specialtyRepository, "Surgery");

            // ==================== 5. SYSTEM ADMINISTRATORS ====================
            // Admin 1 - Main administrator
            ClinicPersonnel admin1 = (ClinicPersonnel) userRepository.save(ClinicPersonnel.builder()
                    .username("admin")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(adminRole)
                    .firstName("Admin")
                    .lastName("DocVanta")
                    .personnelType(PersonnelType.SYSTEM_ADMINISTRATOR)
                    .clinic(clinic)
                    .build());

            // Admin 2 - Secondary administrator
            ClinicPersonnel admin2 = (ClinicPersonnel) userRepository.save(ClinicPersonnel.builder()
                    .username("superadmin")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(adminRole)
                    .firstName("Sara")
                    .lastName("ElMansoury")
                    .personnelType(PersonnelType.SYSTEM_ADMINISTRATOR)
                    .clinic(clinic)
                    .build());

            // ==================== 6. PRACTITIONERS (DOCTORS) ====================
            // Practitioner 1 - Cardiologist
            Practitioner doctor1 = (Practitioner) userRepository.save(Practitioner.builder()
                    .username("dr.hassan")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(practitionerRole)
                    .firstName("Hassan")
                    .lastName("Benali")
                    .email("hassan.benali@docvanta.com")
                    .phone("+212 661-123456")
                    .specialties(new ArrayList<>(List.of(cardioSpec, surgerySpec)))
                    .department(cardiology)
                    .clinic(clinic)
                    .schedules(new ArrayList<>(List.of(
                            PractitionerSchedule.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("TUESDAY").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("WEDNESDAY").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(12, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("THURSDAY").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(16, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("FRIDAY").startTime(LocalTime.of(8, 0)).endTime(LocalTime.of(14, 0)).build()
                    )))
                    .build());

            // Practitioner 2 - Neurologist
            Practitioner doctor2 = (Practitioner) userRepository.save(Practitioner.builder()
                    .username("dr.amina")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(practitionerRole)
                    .firstName("Amina")
                    .lastName("Tazi")
                    .email("amina.tazi@docvanta.com")
                    .phone("+212 662-654321")
                    .specialties(new ArrayList<>(List.of(neuroSpec)))
                    .department(neurology)
                    .clinic(clinic)
                    .schedules(new ArrayList<>(List.of(
                            PractitionerSchedule.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("WEDNESDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(17, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("FRIDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(15, 0)).build()
                    )))
                    .build());

            // Practitioner 3 - General Medicine
            Practitioner doctor3 = (Practitioner) userRepository.save(Practitioner.builder()
                    .username("dr.youssef")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(practitionerRole)
                    .firstName("Youssef")
                    .lastName("Alaoui")
                    .email("youssef.alaoui@docvanta.com")
                    .phone("+212 663-111222")
                    .specialties(new ArrayList<>(List.of(generalSpec, pediatricsSpec)))
                    .department(generalMedicine)
                    .clinic(clinic)
                    .schedules(new ArrayList<>(List.of(
                            PractitionerSchedule.builder().dayOfWeek("MONDAY").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(16, 30)).build(),
                            PractitionerSchedule.builder().dayOfWeek("TUESDAY").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(16, 30)).build(),
                            PractitionerSchedule.builder().dayOfWeek("WEDNESDAY").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(16, 30)).build(),
                            PractitionerSchedule.builder().dayOfWeek("THURSDAY").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(16, 30)).build(),
                            PractitionerSchedule.builder().dayOfWeek("FRIDAY").startTime(LocalTime.of(8, 30)).endTime(LocalTime.of(12, 30)).build()
                    )))
                    .build());

            // Practitioner 4 - Dermatologist
            Practitioner doctor4 = (Practitioner) userRepository.save(Practitioner.builder()
                    .username("dr.fatima")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(practitionerRole)
                    .firstName("Fatima")
                    .lastName("Chraibi")
                    .email("fatima.chraibi@docvanta.com")
                    .phone("+212 664-333444")
                    .specialties(new ArrayList<>(List.of(dermaSpec)))
                    .department(dermatology)
                    .clinic(clinic)
                    .schedules(new ArrayList<>(List.of(
                            PractitionerSchedule.builder().dayOfWeek("TUESDAY").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("THURSDAY").startTime(LocalTime.of(10, 0)).endTime(LocalTime.of(18, 0)).build(),
                            PractitionerSchedule.builder().dayOfWeek("SATURDAY").startTime(LocalTime.of(9, 0)).endTime(LocalTime.of(13, 0)).build()
                    )))
                    .build());

            // ==================== 7. CLINIC PERSONNEL ====================
            // Receptionist
            ClinicPersonnel receptionist = (ClinicPersonnel) userRepository.save(ClinicPersonnel.builder()
                    .username("reception.nadia")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(receptionistRole)
                    .firstName("Nadia")
                    .lastName("Benhaddou")
                    .personnelType(PersonnelType.RECEPTIONIST)
                    .clinic(clinic)
                    .build());

            // ==================== 8. PATIENTS ====================
            Patient patient1 = (Patient) userRepository.save(Patient.builder()
                    .username("patient.ahmed")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(patientRole)
                    .firstName("Ahmed")
                    .lastName("Rachidi")
                    .email("ahmed.rachidi@email.com")
                    .phone("+212 670-111111")
                    .dob(LocalDate.of(1985, 3, 15))
                    .address("45 Rue Mohammed V, Casablanca")
                    .clinic(clinic)
                    .build());

            Patient patient2 = (Patient) userRepository.save(Patient.builder()
                    .username("patient.sarah")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(patientRole)
                    .firstName("Sarah")
                    .lastName("Bounou")
                    .email("sarah.bounou@email.com")
                    .phone("+212 671-222222")
                    .dob(LocalDate.of(1992, 7, 22))
                    .address("12 Avenue Hassan II, Rabat")
                    .clinic(clinic)
                    .build());

            Patient patient3 = (Patient) userRepository.save(Patient.builder()
                    .username("patient.mehdi")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(patientRole)
                    .firstName("Mehdi")
                    .lastName("Tahiri")
                    .email("mehdi.tahiri@email.com")
                    .phone("+212 672-333333")
                    .dob(LocalDate.of(1978, 11, 5))
                    .address("8 Boulevard Zerktouni, Casablanca")
                    .clinic(clinic)
                    .build());

            Patient patient4 = (Patient) userRepository.save(Patient.builder()
                    .username("patient.khadija")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(patientRole)
                    .firstName("Khadija")
                    .lastName("ElAmrani")
                    .email("khadija.elamrani@email.com")
                    .phone("+212 673-444444")
                    .dob(LocalDate.of(2000, 1, 30))
                    .address("22 Rue Allal Ben Abdellah, Fes")
                    .clinic(clinic)
                    .build());

            Patient patient5 = (Patient) userRepository.save(Patient.builder()
                    .username("patient.omar")
                    .password(encodedPassword)
                    .enabled(true)
                    .role(patientRole)
                    .firstName("Omar")
                    .lastName("Benjelloun")
                    .email("omar.benjelloun@email.com")
                    .phone("+212 674-555555")
                    .dob(LocalDate.of(1965, 9, 12))
                    .address("3 Place du 16 Novembre, Marrakech")
                    .clinic(clinic)
                    .build());

            // ==================== 9. PATIENT RECORDS ====================
            patientRecordRepository.save(PatientRecord.builder()
                    .patient(patient1)
                    .bloodType("A+")
                    .allergies("Penicillin, Pollen")
                    .chronicDiseases("Hypertension")
                    .notes("Regular follow-up required. Blood pressure monitored monthly.")
                    .build());

            patientRecordRepository.save(PatientRecord.builder()
                    .patient(patient2)
                    .bloodType("O-")
                    .allergies("None known")
                    .chronicDiseases("None")
                    .notes("Healthy patient. Annual check-up recommended.")
                    .build());

            patientRecordRepository.save(PatientRecord.builder()
                    .patient(patient3)
                    .bloodType("B+")
                    .allergies("Aspirin, Shellfish")
                    .chronicDiseases("Type 2 Diabetes, High Cholesterol")
                    .notes("Diabetic management plan in place. HbA1c to be checked quarterly.")
                    .build());

            patientRecordRepository.save(PatientRecord.builder()
                    .patient(patient5)
                    .bloodType("AB+")
                    .allergies("Latex")
                    .chronicDiseases("Arthritis")
                    .notes("Joint pain management. Physical therapy recommended.")
                    .build());

            // ==================== 10. MEDICAL ACTS (Billing Catalog - before appointments for references) ====================
            MedicalAct actConsGen = medicalActRepository.save(MedicalAct.builder()
                    .code("CONS-GEN").name("General Consultation").description("Standard general practitioner consultation")
                    .category(MedicalActCategory.CONSULTATION).basePrice(new BigDecimal("300.00")).clinic(clinic).build());
            MedicalAct actConsSpec = medicalActRepository.save(MedicalAct.builder()
                    .code("CONS-SPEC").name("Specialist Consultation").description("Specialist doctor consultation")
                    .category(MedicalActCategory.CONSULTATION).basePrice(new BigDecimal("500.00")).clinic(clinic).build());
            MedicalAct actConsFollow = medicalActRepository.save(MedicalAct.builder()
                    .code("CONS-FU").name("Follow-up Consultation").description("Follow-up visit")
                    .category(MedicalActCategory.CONSULTATION).basePrice(new BigDecimal("200.00")).clinic(clinic).build());
            MedicalAct actBloodTest = medicalActRepository.save(MedicalAct.builder()
                    .code("LAB-NFS").name("Complete Blood Count (CBC)").description("Full blood count analysis")
                    .category(MedicalActCategory.LAB_TEST).basePrice(new BigDecimal("150.00")).clinic(clinic).build());
            MedicalAct actGlycemia = medicalActRepository.save(MedicalAct.builder()
                    .code("LAB-GLY").name("Blood Glucose Test").description("Fasting blood glucose test")
                    .category(MedicalActCategory.LAB_TEST).basePrice(new BigDecimal("80.00")).clinic(clinic).build());
            MedicalAct actHbA1c = medicalActRepository.save(MedicalAct.builder()
                    .code("LAB-HBA1C").name("HbA1c Test").description("Glycated hemoglobin test for diabetes monitoring")
                    .category(MedicalActCategory.LAB_TEST).basePrice(new BigDecimal("200.00")).clinic(clinic).build());
            MedicalAct actXRay = medicalActRepository.save(MedicalAct.builder()
                    .code("IMG-XRAY").name("X-Ray").description("Standard X-ray imaging")
                    .category(MedicalActCategory.IMAGING).basePrice(new BigDecimal("400.00")).clinic(clinic).build());
            MedicalAct actECG = medicalActRepository.save(MedicalAct.builder()
                    .code("PROC-ECG").name("Electrocardiogram (ECG)").description("12-lead ECG recording")
                    .category(MedicalActCategory.PROCEDURE).basePrice(new BigDecimal("350.00")).clinic(clinic).build());
            MedicalAct actEcho = medicalActRepository.save(MedicalAct.builder()
                    .code("IMG-ECHO").name("Echocardiogram").description("Cardiac ultrasound")
                    .category(MedicalActCategory.IMAGING).basePrice(new BigDecimal("800.00")).clinic(clinic).build());
            MedicalAct actDermaProc = medicalActRepository.save(MedicalAct.builder()
                    .code("PROC-DERM").name("Dermatological Procedure").description("Skin biopsy or lesion removal")
                    .category(MedicalActCategory.PROCEDURE).basePrice(new BigDecimal("600.00")).clinic(clinic).build());
            medicalActRepository.save(MedicalAct.builder()
                    .code("TRT-INJ").name("Injection / Infusion").description("Intramuscular or IV injection")
                    .category(MedicalActCategory.TREATMENT).basePrice(new BigDecimal("100.00")).clinic(clinic).build());
            medicalActRepository.save(MedicalAct.builder()
                    .code("PROC-SUTURE").name("Wound Suturing").description("Simple wound closure with sutures")
                    .category(MedicalActCategory.PROCEDURE).basePrice(new BigDecimal("450.00")).clinic(clinic).build());
            medicalActRepository.save(MedicalAct.builder()
                    .code("CERT-MED").name("Medical Certificate").description("Medical fitness certificate")
                    .category(MedicalActCategory.OTHER).basePrice(new BigDecimal("100.00")).clinic(clinic).build());

            // ==================== 11. APPOINTMENTS ====================
            LocalDateTime now = LocalDateTime.now();

            // Today's appointments (with estimated price)
            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00001")
                    .datetime(now.withHour(9).withMinute(0).withSecond(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor1).patient(patient1)
                    .reason("Follow-up for hypertension management")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00002")
                    .datetime(now.withHour(10).withMinute(30).withSecond(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor1).patient(patient3)
                    .reason("Cardiac check-up - chest discomfort")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00003")
                    .datetime(now.withHour(11).withMinute(0).withSecond(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor3).patient(patient2)
                    .reason("General health check-up")
                    .estimatedPrice(new BigDecimal("300.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00004")
                    .datetime(now.withHour(14).withMinute(0).withSecond(0))
                    .status(AppointmentStatus.PENDING)
                    .practitioner(doctor2).patient(patient4)
                    .reason("Persistent headaches and dizziness")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00005")
                    .datetime(now.withHour(15).withMinute(30).withSecond(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor4).patient(patient5)
                    .reason("Skin rash evaluation")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            // ── Past appointment: PAID (full workflow completed) ──
            Appointment paidAppt = appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00006")
                    .datetime(now.minusDays(3).withHour(9).withMinute(0))
                    .status(AppointmentStatus.PAID)
                    .paymentStatus(PaymentStatus.PAID)
                    .practitioner(doctor1).patient(patient1)
                    .reason("Blood pressure medication review")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .finalPrice(new BigDecimal("850.00"))
                    .build());

            // Performed acts for this appointment
            performedActRepository.save(PerformedAct.builder()
                    .appointment(paidAppt).medicalAct(actConsSpec).performedBy(doctor1)
                    .quantity(1).unitPrice(new BigDecimal("500.00")).totalPrice(new BigDecimal("500.00"))
                    .performedAt(now.minusDays(3).withHour(9).withMinute(15))
                    .notes("Blood pressure slightly elevated, medication adjustment needed")
                    .build());
            performedActRepository.save(PerformedAct.builder()
                    .appointment(paidAppt).medicalAct(actECG).performedBy(doctor1)
                    .quantity(1).unitPrice(new BigDecimal("350.00")).totalPrice(new BigDecimal("350.00"))
                    .performedAt(now.minusDays(3).withHour(9).withMinute(30))
                    .notes("Normal sinus rhythm, no abnormalities detected")
                    .build());

            // ── Past appointment: BILLED (awaiting payment) ──
            Appointment billedAppt = appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00007")
                    .datetime(now.minusDays(5).withHour(10).withMinute(0))
                    .status(AppointmentStatus.INVOICED)
                    .practitioner(doctor3).patient(patient3)
                    .reason("Diabetes management follow-up")
                    .estimatedPrice(new BigDecimal("300.00"))
                    .finalPrice(new BigDecimal("480.00"))
                    .build());

            performedActRepository.save(PerformedAct.builder()
                    .appointment(billedAppt).medicalAct(actConsFollow).performedBy(doctor3)
                    .quantity(1).unitPrice(new BigDecimal("200.00")).totalPrice(new BigDecimal("200.00"))
                    .performedAt(now.minusDays(5).withHour(10).withMinute(15))
                    .build());
            performedActRepository.save(PerformedAct.builder()
                    .appointment(billedAppt).medicalAct(actHbA1c).performedBy(doctor3)
                    .quantity(1).unitPrice(new BigDecimal("200.00")).totalPrice(new BigDecimal("200.00"))
                    .performedAt(now.minusDays(5).withHour(10).withMinute(30))
                    .build());
            performedActRepository.save(PerformedAct.builder()
                    .appointment(billedAppt).medicalAct(actGlycemia).performedBy(doctor3)
                    .quantity(1).unitPrice(new BigDecimal("80.00")).totalPrice(new BigDecimal("80.00"))
                    .performedAt(now.minusDays(5).withHour(10).withMinute(45))
                    .build());

            // ── Past appointment: COMPLETED (doctor done, awaiting invoice generation) ──
            Appointment completedAppt = appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00008")
                    .datetime(now.minusDays(7).withHour(14).withMinute(0))
                    .status(AppointmentStatus.COMPLETED)
                    .practitioner(doctor2).patient(patient2)
                    .reason("Neurological assessment")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .finalPrice(new BigDecimal("500.00"))
                    .build());

            performedActRepository.save(PerformedAct.builder()
                    .appointment(completedAppt).medicalAct(actConsSpec).performedBy(doctor2)
                    .quantity(1).unitPrice(new BigDecimal("500.00")).totalPrice(new BigDecimal("500.00"))
                    .performedAt(now.minusDays(7).withHour(14).withMinute(15))
                    .notes("Full neurological exam, no significant findings")
                    .build());

            // Past cancelled appointment
            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00009")
                    .datetime(now.minusDays(2).withHour(11).withMinute(0))
                    .status(AppointmentStatus.CANCELLED)
                    .practitioner(doctor4).patient(patient4)
                    .reason("Dermatology consultation")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            // Future appointments
            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00010")
                    .datetime(now.plusDays(1).withHour(9).withMinute(0))
                    .status(AppointmentStatus.PENDING)
                    .practitioner(doctor3).patient(patient4)
                    .reason("General check-up")
                    .estimatedPrice(new BigDecimal("300.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00011")
                    .datetime(now.plusDays(2).withHour(10).withMinute(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor1).patient(patient5)
                    .reason("Cardiac stress test")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00012")
                    .datetime(now.plusDays(3).withHour(14).withMinute(30))
                    .status(AppointmentStatus.PENDING)
                    .practitioner(doctor2).patient(patient1)
                    .reason("Neurological follow-up")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            appointmentRepository.save(Appointment.builder()
                    .referenceNumber("APT-00013")
                    .datetime(now.plusDays(5).withHour(11).withMinute(0))
                    .status(AppointmentStatus.CONFIRMED)
                    .practitioner(doctor4).patient(patient2)
                    .reason("Skin care follow-up")
                    .estimatedPrice(new BigDecimal("500.00"))
                    .build());

            // ==================== 11. MEDICAL DOCUMENTS ====================
            documentRepository.save(MedicalDocument.builder()
                    .type("PRESCRIPTION")
                    .content("Medication: Amlodipine 5mg\nDosage: 1 tablet daily in the morning\nDuration: 3 months\n\nMedication: Lisinopril 10mg\nDosage: 1 tablet daily\nDuration: 3 months\n\nNote: Monitor blood pressure weekly. Return for follow-up in 3 months.")
                    .practitioner(doctor1).patient(patient1)
                    .authorizedForPatient(true)
                    .build());

            documentRepository.save(MedicalDocument.builder()
                    .type("REPORT")
                    .content("CARDIAC ASSESSMENT REPORT\n\nPatient: Ahmed Rachidi\nDate: " + now.minusDays(3).toLocalDate() + "\n\nFindings:\n- Blood pressure: 140/90 mmHg (slightly elevated)\n- Heart rate: 72 bpm (normal)\n- ECG: Normal sinus rhythm\n- No signs of cardiac arrhythmia\n\nRecommendation: Continue current medication. Lifestyle modifications advised (reduced salt intake, regular exercise).")
                    .practitioner(doctor1).patient(patient1)
                    .authorizedForPatient(true)
                    .build());

            documentRepository.save(MedicalDocument.builder()
                    .type("PRESCRIPTION")
                    .content("Medication: Metformin 500mg\nDosage: 1 tablet twice daily with meals\nDuration: 6 months\n\nMedication: Atorvastatin 20mg\nDosage: 1 tablet at bedtime\nDuration: 6 months\n\nNote: HbA1c test in 3 months. Diet control essential.")
                    .practitioner(doctor3).patient(patient3)
                    .authorizedForPatient(true)
                    .build());

            documentRepository.save(MedicalDocument.builder()
                    .type("CERTIFICATE")
                    .content("MEDICAL CERTIFICATE\n\nI, Dr. Amina Tazi, certify that Ms. Sarah Bounou has been examined on " + now.minusDays(7).toLocalDate() + " and is declared fit for work.\n\nThe patient underwent a neurological assessment with no significant findings.\n\nThis certificate is issued at the request of the patient for employment purposes.")
                    .practitioner(doctor2).patient(patient2)
                    .authorizedForPatient(true)
                    .build());

            documentRepository.save(MedicalDocument.builder()
                    .type("REPORT")
                    .content("DIABETES MANAGEMENT REPORT\n\nPatient: Mehdi Tahiri\nDate: " + now.minusDays(5).toLocalDate() + "\n\nLab Results:\n- Fasting glucose: 145 mg/dL (elevated)\n- HbA1c: 7.2% (above target)\n- Total cholesterol: 220 mg/dL\n- LDL: 140 mg/dL\n\nPlan: Increase Metformin dosage. Nutritional counseling referral. Recheck in 3 months.")
                    .practitioner(doctor3).patient(patient3)
                    .authorizedForPatient(false)
                    .build());

            documentRepository.save(MedicalDocument.builder()
                    .type("PRESCRIPTION")
                    .content("Medication: Ibuprofen 400mg\nDosage: 1 tablet as needed, max 3 per day\nDuration: 2 weeks\n\nMedication: Glucosamine 1500mg\nDosage: 1 tablet daily\nDuration: 3 months\n\nNote: Avoid prolonged standing. Apply warm compresses to affected joints.")
                    .practitioner(doctor3).patient(patient5)
                    .authorizedForPatient(true)
                    .build());

            // ==================== 12. SAMPLE INVOICES (linked to appointments) ====================
            // Invoice 1: PAID - linked to paidAppt (cardiac consultation)
            Invoice inv1 = invoiceRepository.save(Invoice.builder()
                    .invoiceNumber("INV-" + LocalDate.now().minusDays(3).toString().replace("-", "") + "-0001")
                    .patient(patient1).appointment(paidAppt).createdBy(receptionist).clinic(clinic)
                    .createdAt(now.minusDays(3).withHour(10).withMinute(0))
                    .status(InvoiceStatus.PAID)
                    .totalAmount(new BigDecimal("850.00")).paidAmount(new BigDecimal("850.00")).remainingAmount(BigDecimal.ZERO)
                    .build());
            invoiceLineRepository.save(InvoiceLine.builder().invoice(inv1).medicalAct(actConsSpec)
                    .description("Specialist Consultation").quantity(1).unitPrice(new BigDecimal("500.00")).lineTotal(new BigDecimal("500.00")).build());
            invoiceLineRepository.save(InvoiceLine.builder().invoice(inv1).medicalAct(actECG)
                    .description("Electrocardiogram (ECG)").quantity(1).unitPrice(new BigDecimal("350.00")).lineTotal(new BigDecimal("350.00")).build());
            paymentRepository.save(Payment.builder().invoice(inv1).amount(new BigDecimal("850.00"))
                    .paymentMethod(PaymentMethod.CARD).paymentDate(now.minusDays(3).withHour(10).withMinute(30))
                    .receivedBy(receptionist).reference("CARD-TX-001").build());

            // Invoice 2: UNPAID - linked to billedAppt (diabetes follow-up, awaiting payment)
            Invoice inv2 = invoiceRepository.save(Invoice.builder()
                    .invoiceNumber("INV-" + LocalDate.now().minusDays(5).toString().replace("-", "") + "-0001")
                    .patient(patient3).appointment(billedAppt).createdBy(receptionist).clinic(clinic)
                    .createdAt(now.minusDays(5).withHour(11).withMinute(0))
                    .status(InvoiceStatus.UNPAID)
                    .totalAmount(new BigDecimal("480.00")).paidAmount(BigDecimal.ZERO).remainingAmount(new BigDecimal("480.00"))
                    .build());
            invoiceLineRepository.save(InvoiceLine.builder().invoice(inv2).medicalAct(actConsFollow)
                    .description("Follow-up Consultation").quantity(1).unitPrice(new BigDecimal("200.00")).lineTotal(new BigDecimal("200.00")).build());
            invoiceLineRepository.save(InvoiceLine.builder().invoice(inv2).medicalAct(actHbA1c)
                    .description("HbA1c Test").quantity(1).unitPrice(new BigDecimal("200.00")).lineTotal(new BigDecimal("200.00")).build());
            invoiceLineRepository.save(InvoiceLine.builder().invoice(inv2).medicalAct(actGlycemia)
                    .description("Blood Glucose Test").quantity(1).unitPrice(new BigDecimal("80.00")).lineTotal(new BigDecimal("80.00")).build());

            // Note: completedAppt has NO invoice yet — receptionist can generate it via POST /api/invoices/generate/{id}

            System.out.println("============================================");
            System.out.println("  DOCVANTA - Data Initialization Complete");
            System.out.println("============================================");
            System.out.println("  All passwords: password123");
            System.out.println("--------------------------------------------");
            System.out.println("  SYSTEM ADMINISTRATORS:");
            System.out.println("    admin          (Admin DocVanta)");
            System.out.println("    superadmin     (Sara ElMansoury)");
            System.out.println("  PRACTITIONERS:");
            System.out.println("    dr.hassan      (Cardiology/Surgery)");
            System.out.println("    dr.amina       (Neurology)");
            System.out.println("    dr.youssef     (General/Pediatrics)");
            System.out.println("    dr.fatima      (Dermatology)");
            System.out.println("  RECEPTIONIST:");
            System.out.println("    reception.nadia  (RECEPTIONIST)");
            System.out.println("  PATIENTS:");
            System.out.println("    patient.ahmed    (Ahmed Rachidi)");
            System.out.println("    patient.sarah    (Sarah Bounou)");
            System.out.println("    patient.mehdi    (Mehdi Tahiri)");
            System.out.println("    patient.khadija  (Khadija ElAmrani)");
            System.out.println("    patient.omar     (Omar Benjelloun)");
            System.out.println("============================================");
            System.out.println("  Seeded: 1 Clinic, 5 Departments,");
            System.out.println("  6 Specialties, 13 Appointments,");
            System.out.println("  6 Documents, 4 Patient Records,");
            System.out.println("  13 Medical Acts, 2 Invoices,");
            System.out.println("  6 Performed Acts");
            System.out.println("  Workflow demo:");
            System.out.println("    - paidAppt: PAID (full cycle)");
            System.out.println("    - billedAppt: BILLED (UNPAID invoice)");
            System.out.println("    - completedAppt: COMPLETED (ready for invoice)");
            System.out.println("============================================");
        };
    }

    private void createRoleIfNotExists(UserRoleRepository userRoleRepository, AccessRightRepository accessRightRepository,
                                       String name, List<AccessRight> accessRights) {
        if (userRoleRepository.findByName(name).isEmpty()) {
            List<AccessRight> managedAccessRights = accessRights.stream()
                    .map(ar -> accessRightRepository.findByCode(ar.getCode())
                            .orElseGet(() -> accessRightRepository.save(ar)))
                    .collect(Collectors.toList());

            UserRole role = UserRole.builder()
                    .name(name)
                    .accessRights(managedAccessRights)
                    .build();
            userRoleRepository.save(role);
        }
    }

    private AccessRight accessRight(String code, String description) {
        return AccessRight.builder()
                .code(code)
                .description(description)
                .build();
    }

    private Specialty getOrCreateSpecialty(SpecialtyRepository repo, String name) {
        return repo.findByName(name).orElseGet(() -> repo.save(Specialty.builder().name(name).build()));
    }
}




