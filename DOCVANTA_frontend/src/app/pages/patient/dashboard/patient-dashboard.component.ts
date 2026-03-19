import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { PatientService } from '../../../services/patient.service';
import { AppointmentService } from '../../../services/appointment.service';
import { DocumentService } from '../../../services/document.service';
import { MedicalRecordService } from '../../../services/medical-record.service';
import { Appointment } from '../../../models/auth.models';
import { AppointmentDetailModalComponent, AppointmentAction } from '../../../components/appointment-detail-modal/appointment-detail-modal.component';

@Component({
    selector: 'app-patient-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, AppointmentDetailModalComponent],
    templateUrl: './patient-dashboard.component.html',
    styleUrls: ['./patient-dashboard.component.css']
})
export class PatientDashboardComponent implements OnInit {
    user: any;
    patient: any = null;
    upcomingAppointments: any[] = [];
    recentDocuments: any[] = [];
    medicalRecord: any = null;
    loading = true;
    selectedAppointment: Appointment | null = null;
    processingAction = false;

    stats = {
        upcoming: 0,
        total: 0,
        documents: 0,
        hasRecord: false
    };

    // Track how many API calls have completed (success or error)
    private completedCalls = 0;
    private readonly totalCalls = 4;

    constructor(
        private authService: AuthService,
        private patientService: PatientService,
        private appointmentService: AppointmentService,
        private documentService: DocumentService,
        private medicalRecordService: MedicalRecordService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadData();
    }

    private checkAllDone(): void {
        this.completedCalls++;
        if (this.completedCalls >= this.totalCalls) {
            this.stats = {
                upcoming: this.upcomingAppointments.length,
                total: this.stats.total,
                documents: this.recentDocuments.length,
                hasRecord: !!this.medicalRecord
            };
            this.loading = false;
            this.cdr.detectChanges();
        }
    }

    private loadData(): void {
        const userId = this.user?.userId;
        if (!userId) {
            this.loading = false;
            return;
        }

        // Reset state
        this.completedCalls = 0;
        this.loading = true;

        // 1. Load patient info
        this.patientService.getById(userId).subscribe({
            next: (data) => { this.patient = data; this.checkAllDone(); },
            error: () => { this.patient = null; this.checkAllDone(); }
        });

        // 2. Load appointments
        this.appointmentService.getByPatient(userId).subscribe({
            next: (data) => {
                const allAppointments = data || [];
                const now = new Date();
                this.upcomingAppointments = allAppointments
                    .filter((a: any) => new Date(a.datetime) >= now && a.status !== 'CANCELLED')
                    .sort((a: any, b: any) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime())
                    .slice(0, 5);
                this.stats.total = allAppointments.length;
                this.checkAllDone();
            },
            error: () => { this.upcomingAppointments = []; this.checkAllDone(); }
        });

        // 3. Load authorized documents
        this.documentService.getAuthorizedForPatient(userId).subscribe({
            next: (data) => {
                this.recentDocuments = (data || []).slice(0, 4);
                this.checkAllDone();
            },
            error: () => { this.recentDocuments = []; this.checkAllDone(); }
        });

        // 4. Load medical record (may return 400/404 if no record exists — that's OK)
        this.medicalRecordService.getByPatient(userId).subscribe({
            next: (data) => { this.medicalRecord = data; this.checkAllDone(); },
            error: () => { this.medicalRecord = null; this.checkAllDone(); }
        });
    }

    getInitials(): string {
        if (this.patient?.firstName && this.patient?.lastName) {
            return (this.patient.firstName.charAt(0) + this.patient.lastName.charAt(0)).toUpperCase();
        }
        return this.user?.username?.charAt(0)?.toUpperCase() || 'P';
    }

    getStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'badge-info';
            case 'CONFIRMED': return 'badge-success';
            case 'CANCELLED': return 'badge-danger';
            case 'COMPLETED': return 'badge-secondary';
            default: return 'badge-warning';
        }
    }

    getDocTypeClass(type: string): string {
        switch (type?.toUpperCase()) {
            case 'CERTIFICATE': return 'doc-type-cert';
            case 'PRESCRIPTION': return 'doc-type-presc';
            case 'REPORT': return 'doc-type-report';
            default: return 'doc-type-other';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '';
        const d = new Date(dateStr);
        return d.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    getDay(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).getDate().toString();
    }

    getMonth(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' });
    }

    openAppointmentDetail(apt: Appointment): void {
        this.selectedAppointment = apt;
    }

    closeAppointmentDetail(): void {
        this.selectedAppointment = null;
    }

    onAppointmentAction(event: AppointmentAction): void {
        this.processingAction = true;
        let obs;
        switch (event.type) {
            case 'cancel': obs = this.appointmentService.cancel(event.appointmentId); break;
            default: return;
        }
        obs.subscribe({
            next: () => { this.processingAction = false; this.selectedAppointment = null; this.loadData(); },
            error: () => { this.processingAction = false; }
        });
    }
}
