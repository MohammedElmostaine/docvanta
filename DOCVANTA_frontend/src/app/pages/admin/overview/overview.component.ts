import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { PractitionerService } from '../../../services/practitioner.service';
import { PatientService } from '../../../services/patient.service';
import { AppointmentService } from '../../../services/appointment.service';
import { DocumentService } from '../../../services/document.service';
import { ClinicPersonnelService } from '../../../services/clinic-personnel.service';
import { Appointment } from '../../../models/auth.models';
import { AppointmentDetailModalComponent, AppointmentAction } from '../../../components/appointment-detail-modal/appointment-detail-modal.component';

@Component({
    selector: 'app-admin-overview',
    standalone: true,
    imports: [CommonModule, RouterLink, AppointmentDetailModalComponent],
    templateUrl: './overview.component.html',
    styleUrls: ['./overview.component.css']
})
export class AdminOverviewComponent implements OnInit {
    stats = {
        practitioners: 0,
        patients: 0,
        appointments: 0,
        documents: 0,
        personnel: 0
    };
    recentAppointments: any[] = [];
    loading = true;
    selectedAppointment: Appointment | null = null;
    processingAction = false;

    constructor(
        private practitionerService: PractitionerService,
        private patientService: PatientService,
        private appointmentService: AppointmentService,
        private documentService: DocumentService,
        private clinicPersonnelService: ClinicPersonnelService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadStats();
    }

    loadStats(): void {
        this.loading = true;
        let completed = 0;
        const checkDone = () => { completed++; if (completed >= 5) { this.loading = false; this.cdr.detectChanges(); } };

        this.practitionerService.getAll().subscribe({
            next: d => { this.stats.practitioners = d?.length || 0; checkDone(); },
            error: () => checkDone()
        });

        this.patientService.getAll().subscribe({
            next: p => { this.stats.patients = p?.length || 0; checkDone(); },
            error: () => checkDone()
        });

        this.appointmentService.getAll().subscribe({
            next: a => {
                this.stats.appointments = a?.length || 0;
                this.recentAppointments = (a || []).slice(0, 5);
                checkDone();
            },
            error: () => checkDone()
        });

        this.documentService.getAll().subscribe({
            next: d => { this.stats.documents = d?.length || 0; checkDone(); },
            error: () => checkDone()
        });

        this.clinicPersonnelService.getAll().subscribe({
            next: s => { this.stats.personnel = s?.length || 0; checkDone(); },
            error: () => checkDone()
        });
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
            next: () => { this.processingAction = false; this.selectedAppointment = null; this.loadStats(); },
            error: () => { this.processingAction = false; }
        });
    }
}
