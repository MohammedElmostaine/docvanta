import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { HttpClient } from '@angular/common/http';
import { PatientService } from '../../../services/patient.service';
import { AppointmentService } from '../../../services/appointment.service';
import { API_BASE } from '../../../config/api.config';
import { Patient, Appointment } from '../../../models/auth.models';

@Component({
    selector: 'app-staff-patients',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './staff-patients.component.html',
    styleUrls: ['./staff-patients.component.css']
})
export class StaffPatientsComponent implements OnInit {
    patients: Patient[] = [];
    filteredPatients: Patient[] = [];
    todayAppointments: Appointment[] = [];
    loading = true;
    searchQuery = '';
    errorMessage = '';
    successMessage = '';

    // Patient detail modal
    showDetailModal = false;
    selectedPatient: Patient | null = null;
    patientAppointments: Appointment[] = [];
    loadingPatientDetails = false;

    // Quick registration modal
    showRegisterModal = false;
    newPatient = {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        dob: '',
        address: ''
    };
    registerError = '';
    registering = false;

    get pendingCount(): number {
        return this.todayAppointments.filter(a => a.status === 'PENDING').length;
    }

    get checkedInCount(): number {
        return this.todayAppointments.filter(a => a.status === 'CONFIRMED').length;
    }

    private allScopedAppointments: Appointment[] = [];

    constructor(
        private patientService: PatientService,
        private appointmentService: AppointmentService,
        private http: HttpClient,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const checkDone = () => {
            completed++;
            if (completed >= 2) {
                this.loading = false;
                this.cdr.detectChanges();
            }
        };

        this.appointmentService.getAll().subscribe({
            next: (appointments) => {
                this.allScopedAppointments = appointments || [];
                const today = new Date();
                today.setHours(0, 0, 0, 0);
                const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);

                this.todayAppointments = this.allScopedAppointments.filter((apt: Appointment) => {
                    const aptDate = new Date(apt.datetime);
                    return aptDate >= today && aptDate < tomorrow;
                });

                // Load all patients then filter to only those with scoped appointments
                this.patientService.getAll().subscribe({
                    next: (data) => {
                        const scopedPatientIds = new Set(this.allScopedAppointments.map(a => a.patientId));
                        this.patients = (data || []).filter(p => scopedPatientIds.has(p.userId));
                        this.filteredPatients = [...this.patients];
                        checkDone();
                    },
                    error: (err) => {
                        this.errorMessage = err.error?.message || 'Failed to load patients';
                        checkDone();
                    }
                });

                checkDone();
            },
            error: () => { checkDone(); checkDone(); }
        });
    }

    onSearch(): void {
        const q = this.searchQuery.toLowerCase().trim();
        if (!q) {
            this.filteredPatients = [...this.patients];
            return;
        }
        this.filteredPatients = this.patients.filter(p =>
            (p.firstName + ' ' + p.lastName).toLowerCase().includes(q) ||
            p.email?.toLowerCase().includes(q) ||
            p.phone?.includes(q)
        );
    }

    hasAppointmentToday(patientId: number): boolean {
        return this.todayAppointments.some(apt => apt.patientId === patientId);
    }

    getPatientTodayAppointment(patientId: number): Appointment | undefined {
        return this.todayAppointments.find(apt => apt.patientId === patientId);
    }

    checkInPatient(patientId: number): void {
        const appointment = this.getPatientTodayAppointment(patientId);
        if (appointment && appointment.status === 'PENDING') {
            this.appointmentService.updateStatus(appointment.appointmentId, 'CONFIRMED').subscribe({
                next: () => {
                    this.successMessage = 'Patient checked in successfully!';
                    this.loadData();
                    setTimeout(() => this.successMessage = '', 3000);
                },
                error: () => { this.errorMessage = 'Failed to check in patient'; this.cdr.detectChanges(); }
            });
        }
    }

    viewPatientDetails(patient: Patient): void {
        this.selectedPatient = patient;
        this.showDetailModal = true;
        this.loadingPatientDetails = true;

        // Use already-loaded scoped appointments instead of fetching all
        this.patientAppointments = this.allScopedAppointments
            .filter((apt: Appointment) => apt.patientId === patient.userId)
            .sort((a: Appointment, b: Appointment) =>
                new Date(b.datetime).getTime() - new Date(a.datetime).getTime()
            )
            .slice(0, 10);
        this.loadingPatientDetails = false;
    }

    closeDetailModal(): void {
        this.showDetailModal = false;
        this.selectedPatient = null;
        this.patientAppointments = [];
    }

    openRegisterModal(): void {
        this.showRegisterModal = true;
        this.registerError = '';
        this.newPatient = { firstName: '', lastName: '', email: '', phone: '', dob: '', address: '' };
    }

    closeRegisterModal(): void {
        this.showRegisterModal = false;
    }

    registerPatient(): void {
        if (!this.newPatient.firstName || !this.newPatient.lastName) {
            this.registerError = 'First name and last name are required.';
            return;
        }
        this.registering = true;
        this.registerError = '';

        const registerRequest = {
            username: (this.newPatient.firstName + this.newPatient.lastName).toLowerCase().replace(/\s+/g, ''),
            password: 'password123',
            firstName: this.newPatient.firstName,
            lastName: this.newPatient.lastName,
            email: this.newPatient.email || '',
            phone: this.newPatient.phone || '',
            dob: this.newPatient.dob || '',
            address: this.newPatient.address || '',
            role: 'PATIENT'
        };

        this.http.post(`${API_BASE}/auth/register`, registerRequest).subscribe({
            next: () => {
                this.registering = false;
                this.showRegisterModal = false;
                this.successMessage = `Patient ${this.newPatient.firstName} ${this.newPatient.lastName} registered successfully! (Default password: password123)`;
                this.loadData();
                setTimeout(() => this.successMessage = '', 5000);
            },
            error: (err: any) => {
                this.registering = false;
                this.registerError = err.error?.message || 'Failed to register patient. They may need to use the registration page.';
            }
        });
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-US', {
            month: 'short',
            day: 'numeric',
            year: 'numeric'
        });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleTimeString('en-US', {
            hour: '2-digit',
            minute: '2-digit'
        });
    }

    getStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'status-pending';
            case 'CONFIRMED': return 'status-confirmed';
            case 'CANCELLED': return 'status-cancelled';
            case 'COMPLETED': return 'status-completed';
            default: return 'status-pending';
        }
    }

    calculateAge(dob: string): number {
        if (!dob) return 0;
        const birthDate = new Date(dob);
        const today = new Date();
        let age = today.getFullYear() - birthDate.getFullYear();
        const monthDiff = today.getMonth() - birthDate.getMonth();
        if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
            age--;
        }
        return age;
    }
}
