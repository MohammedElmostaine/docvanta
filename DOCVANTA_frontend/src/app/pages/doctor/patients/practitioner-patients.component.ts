import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { PatientService } from '../../../services/patient.service';
import { Patient, Appointment } from '../../../models/auth.models';

@Component({
    selector: 'app-practitioner-patients',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './practitioner-patients.component.html',
    styleUrls: ['./practitioner-patients.component.css']
})
export class PractitionerPatientsComponent implements OnInit {
    user: any;
    patients: Patient[] = [];
    filteredPatients: Patient[] = [];
    loading = true;
    errorMessage = '';
    searchQuery = '';
    selectedPatient: Patient | null = null;
    patientAppointments: Appointment[] = [];

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private patientService: PatientService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadPatients();
    }

    loadPatients(): void {
        this.loading = true;
        const practitionerId = this.user?.userId;
        if (!practitionerId) { this.loading = false; return; }

        this.appointmentService.getByPractitioner(practitionerId).subscribe({
            next: (appointments) => {
                const patientIds = new Set<number>();
                const patientMap = new Map<number, { id: number; name: string }>();
                (appointments || []).forEach(a => {
                    if (!patientIds.has(a.patientId)) {
                        patientIds.add(a.patientId);
                        patientMap.set(a.patientId, { id: a.patientId, name: a.patientName });
                    }
                });

                if (patientIds.size === 0) {
                    this.patients = [];
                    this.filteredPatients = [];
                    this.loading = false;
                    this.cdr.detectChanges();
                    return;
                }

                // Load full patient details
                this.patientService.getAll().subscribe({
                    next: (allPatients) => {
                        this.patients = (allPatients || []).filter(p => patientIds.has(p.userId));
                        this.filteredPatients = [...this.patients];
                        this.loading = false;
                        this.cdr.detectChanges();
                    },
                    error: () => {
                        // Fallback to appointment-derived data
                        this.patients = Array.from(patientMap.values()).map(p => ({
                            userId: p.id, username: '', firstName: p.name.split(' ')[0] || '',
                            lastName: p.name.split(' ').slice(1).join(' ') || '', dob: '', phone: '',
                            email: '', address: '', enabled: true
                        })) as Patient[];
                        this.filteredPatients = [...this.patients];
                        this.loading = false;
                        this.cdr.detectChanges();
                    }
                });
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load patients';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilter(): void {
        const q = this.searchQuery.toLowerCase();
        this.filteredPatients = this.patients.filter(p =>
            (p.firstName + ' ' + p.lastName).toLowerCase().includes(q) ||
            (p.email || '').toLowerCase().includes(q) ||
            (p.phone || '').toLowerCase().includes(q)
        );
    }

    viewPatient(patient: Patient): void {
        this.selectedPatient = patient;
        this.patientAppointments = [];
        this.appointmentService.getByPatient(patient.userId).subscribe({
            next: (appointments) => {
                this.patientAppointments = (appointments || [])
                    .filter(a => a.practitionerId === this.user?.userId)
                    .sort((a, b) => new Date(b.datetime).getTime() - new Date(a.datetime).getTime());
            }
        });
    }

    closeDetail(): void { this.selectedPatient = null; }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'CONFIRMED': return 'badge-success';
            case 'PENDING': return 'badge-warning';
            case 'CANCELLED': case 'REJECTED': return 'badge-danger';
            case 'COMPLETED': return 'badge-info';
            default: return 'badge-secondary';
        }
    }

    getInitials(patient: Patient): string {
        return ((patient.firstName?.[0] || '') + (patient.lastName?.[0] || '')).toUpperCase();
    }
}
