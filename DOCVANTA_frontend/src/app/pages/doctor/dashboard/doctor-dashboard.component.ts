import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { PatientService } from '../../../services/patient.service';
import { DocumentService } from '../../../services/document.service';
import { Appointment, Patient, MedicalDocument } from '../../../models/auth.models';
import { AppointmentDetailModalComponent, AppointmentAction } from '../../../components/appointment-detail-modal/appointment-detail-modal.component';

@Component({
    selector: 'app-doctor-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, AppointmentDetailModalComponent],
    templateUrl: './doctor-dashboard.component.html',
    styleUrls: ['./doctor-dashboard.component.css']
})
export class DoctorDashboardComponent implements OnInit {
    user: any;
    todayAppointments: Appointment[] = [];
    upcomingAppointments: Appointment[] = [];
    myPatients: Patient[] = [];
    recentDocuments: MedicalDocument[] = [];
    stats = { patients: 0, todayAppointments: 0, totalAppointments: 0, documents: 0 };
    loading = true;
    selectedAppointment: Appointment | null = null;
    processingAction = false;

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private patientService: PatientService,
        private documentService: DocumentService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const total = 3;
        const checkDone = () => { completed++; if (completed >= total) { this.loading = false; this.cdr.detectChanges(); } };

        const doctorId = this.user?.userId;
        if (!doctorId) { this.loading = false; return; }

        this.appointmentService.getByPractitioner(doctorId).subscribe({
            next: appointments => {
                const all = appointments || [];
                this.stats.totalAppointments = all.length;
                const today = new Date().toISOString().split('T')[0];
                this.todayAppointments = all.filter(a => a.datetime?.startsWith(today));
                this.stats.todayAppointments = this.todayAppointments.length;
                this.upcomingAppointments = all
                    .filter(a => a.status !== 'CANCELLED' && new Date(a.datetime) >= new Date())
                    .sort((a, b) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime())
                    .slice(0, 5);
                checkDone();
            },
            error: () => checkDone()
        });

        this.patientService.getAll().subscribe({
            next: patients => {
                this.myPatients = (patients || []).slice(0, 5);
                this.stats.patients = (patients || []).length;
                checkDone();
            },
            error: () => checkDone()
        });

        this.documentService.getByPractitioner(doctorId).subscribe({
            next: docs => {
                this.recentDocuments = (docs || []).slice(0, 5);
                this.stats.documents = (docs || []).length;
                checkDone();
            },
            error: () => checkDone()
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'CONFIRMED': return 'badge-success';
            case 'PENDING': return 'badge-warning';
            case 'CANCELLED': case 'REJECTED': return 'badge-danger';
            case 'COMPLETED': return 'badge-info';
            case 'INVOICED': return 'badge-billed';
            case 'PAID': return 'badge-paid';
            default: return 'badge-secondary';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
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
            case 'confirm': obs = this.appointmentService.confirm(event.appointmentId); break;
            case 'reject': obs = this.appointmentService.reject(event.appointmentId); break;
            case 'complete': obs = this.appointmentService.complete(event.appointmentId); break;
            case 'cancel': obs = this.appointmentService.cancel(event.appointmentId); break;
        }
        obs.subscribe({
            next: () => { this.processingAction = false; this.selectedAppointment = null; this.loadData(); },
            error: () => { this.processingAction = false; }
        });
    }
}
