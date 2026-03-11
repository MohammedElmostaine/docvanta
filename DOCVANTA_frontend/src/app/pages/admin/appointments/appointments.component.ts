import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../services/appointment.service';
import { PractitionerService } from '../../../services/practitioner.service';
import { PatientService } from '../../../services/patient.service';
import { Appointment, AppointmentRequest, Practitioner, Patient } from '../../../models/auth.models';

@Component({
    selector: 'app-appointments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './appointments.component.html',
    styleUrls: ['./appointments.component.css']
})
export class AppointmentsComponent implements OnInit {
    appointments: Appointment[] = [];
    filteredAppointments: Appointment[] = [];
    practitioners: Practitioner[] = [];
    patients: Patient[] = [];
    loading = true;
    searchQuery = '';
    statusFilter = '';
    errorMessage = '';

    showModal = false;
    editingId: number | null = null;
    formData: AppointmentRequest = { datetime: '', practitionerId: 0, patientId: 0 };

    statuses = ['PENDING', 'CONFIRMED', 'COMPLETED', 'INVOICED', 'PAID', 'CANCELLED', 'REJECTED'];

    constructor(
        private appointmentService: AppointmentService,
        private practitionerService: PractitionerService,
        private patientService: PatientService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let done = 0;
        const check = () => { done++; if (done >= 3) { this.loading = false; this.cdr.detectChanges(); } };

        this.appointmentService.getAll().subscribe({
            next: d => { this.appointments = d || []; this.applyFilters(); check(); },
            error: () => check()
        });
        this.practitionerService.getAll().subscribe({
            next: d => { this.practitioners = d || []; check(); },
            error: () => check()
        });
        this.patientService.getAll().subscribe({
            next: p => { this.patients = p || []; check(); },
            error: () => check()
        });
    }

    applyFilters(): void {
        let list = [...this.appointments];
        const q = this.searchQuery.toLowerCase().trim();
        if (q) {
            list = list.filter(a =>
                a.practitionerName?.toLowerCase().includes(q) ||
                a.patientName?.toLowerCase().includes(q)
            );
        }
        if (this.statusFilter) {
            list = list.filter(a => a.status === this.statusFilter);
        }
        this.filteredAppointments = list;
    }

    onSearch(): void { this.applyFilters(); }
    onStatusFilter(): void { this.applyFilters(); }

    openCreateModal(): void {
        this.editingId = null;
        this.formData = { datetime: '', practitionerId: 0, patientId: 0 };
        this.showModal = true;
    }

    openEditModal(apt: Appointment): void {
        this.editingId = apt.appointmentId;
        this.formData = {
            datetime: apt.datetime?.substring(0, 16) || '',
            practitionerId: apt.practitionerId,
            patientId: apt.patientId,
            status: apt.status
        };
        this.showModal = true;
    }

    closeModal(): void {
        this.showModal = false;
    }

    saveAppointment(): void {
        const req = { ...this.formData };
        if (this.editingId) {
            this.appointmentService.update(this.editingId, req).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to update appointment'; this.loading = false; }
            });
        } else {
            this.appointmentService.create(req).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to create appointment'; this.loading = false; }
            });
        }
    }

    updateStatus(id: number, status: string): void {
        this.appointmentService.updateStatus(id, status).subscribe({
            next: () => this.loadData(),
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to update status'; this.loading = false; }
        });
    }

    deleteAppointment(id: number): void {
        if (confirm('Delete this appointment?')) {
            this.appointmentService.delete(id).subscribe({
                next: () => this.loadData(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.loading = false; }
            });
        }
    }

    formatDate(dt: string): string {
        if (!dt) return 'N/A';
        return new Date(dt).toLocaleString();
    }
}
