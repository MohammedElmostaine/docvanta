import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { PractitionerService } from '../../../services/practitioner.service';
import { Appointment } from '../../../models/auth.models';
import { AppointmentDetailModalComponent, AppointmentAction, DashboardRole } from '../../../components/appointment-detail-modal/appointment-detail-modal.component';

@Component({
    selector: 'app-staff-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, AppointmentDetailModalComponent],
    templateUrl: './staff-dashboard.component.html',
    styleUrls: ['./staff-dashboard.component.css']
})
export class StaffDashboardComponent implements OnInit {
    user: any;
    staffInfo: any = null;
    loading = true;
    userRole: DashboardRole = 'RECEPTIONIST';
    basePath = '/receptionist';

    stats = {
        totalToday: 0,
        pending: 0,
        confirmed: 0,
        completed: 0,
        cancelled: 0,
        totalPatients: 0,
        totalPractitioners: 0,
        awaitingCheckIn: 0
    };

    upcomingAppointments: any[] = [];
    patientsAwaitingCheckIn: any[] = [];
    currentTime = new Date();
    processingIds = new Set<number>();
    selectedAppointment: Appointment | null = null;
    processingAction = false;

    private completedCalls = 0;
    private totalCalls = 2;

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private practitionerService: PractitionerService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.userRole = (this.user?.role || 'RECEPTIONIST') as DashboardRole;
        this.loadData();
        setInterval(() => {
            this.currentTime = new Date();
            this.cdr.detectChanges();
        }, 60000);
    }

    private checkAllDone(): void {
        this.completedCalls++;
        if (this.completedCalls >= this.totalCalls) {
            this.loading = false;
            this.cdr.detectChanges();
        }
    }

    private loadData(): void {
        this.completedCalls = 0;
        this.loading = true;

        this.appointmentService.getAll().subscribe({
            next: (appointments: Appointment[]) => {
                const all = appointments || [];
                const now = new Date();
                const today = new Date(now.getFullYear(), now.getMonth(), now.getDate());
                const tomorrow = new Date(today.getTime() + 24 * 60 * 60 * 1000);

                const todayAppointments = all.filter((a: Appointment) => {
                    const d = new Date(a.datetime);
                    return d >= today && d < tomorrow;
                });

                this.stats.totalToday = todayAppointments.length;
                this.stats.pending = todayAppointments.filter((a: Appointment) => a.status === 'PENDING').length;
                this.stats.confirmed = todayAppointments.filter((a: Appointment) => a.status === 'CONFIRMED').length;
                this.stats.completed = todayAppointments.filter((a: Appointment) => a.status === 'COMPLETED').length;
                this.stats.cancelled = todayAppointments.filter((a: Appointment) => a.status === 'CANCELLED').length;

                const uniquePatientIds = new Set(all.map((a: Appointment) => a.patientId));
                this.stats.totalPatients = uniquePatientIds.size;

                this.patientsAwaitingCheckIn = todayAppointments
                    .filter((a: Appointment) => a.status === 'PENDING')
                    .sort((a: Appointment, b: Appointment) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime())
                    .slice(0, 5);
                this.stats.awaitingCheckIn = this.patientsAwaitingCheckIn.length;

                this.upcomingAppointments = all
                    .filter((a: Appointment) => new Date(a.datetime) >= now && a.status !== 'CANCELLED')
                    .sort((a: Appointment, b: Appointment) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime())
                    .slice(0, 8);

                this.checkAllDone();
            },
            error: () => { this.checkAllDone(); }
        });

        this.practitionerService.getAll().subscribe({
            next: (practitioners: any[]) => {
                this.stats.totalPractitioners = (practitioners || []).length;
                this.checkAllDone();
            },
            error: () => { this.checkAllDone(); }
        });
    }

    isProcessing(id: number): boolean {
        return this.processingIds.has(id);
    }

    confirmAppointment(id: number): void {
        if (this.processingIds.has(id)) return;
        this.processingIds.add(id);
        this.appointmentService.updateStatus(id, 'CONFIRMED').subscribe({
            next: () => { this.processingIds.delete(id); this.loadData(); },
            error: () => { this.processingIds.delete(id); this.cdr.detectChanges(); }
        });
    }

    cancelAppointment(id: number): void {
        if (this.processingIds.has(id)) return;
        this.processingIds.add(id);
        this.appointmentService.updateStatus(id, 'CANCELLED').subscribe({
            next: () => { this.processingIds.delete(id); this.loadData(); },
            error: () => { this.processingIds.delete(id); this.cdr.detectChanges(); }
        });
    }

    completeAppointment(id: number): void {
        if (this.processingIds.has(id)) return;
        this.processingIds.add(id);
        this.appointmentService.updateStatus(id, 'COMPLETED').subscribe({
            next: () => { this.processingIds.delete(id); this.loadData(); },
            error: () => { this.processingIds.delete(id); this.cdr.detectChanges(); }
        });
    }

    getStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'badge-warning';
            case 'CONFIRMED': return 'badge-success';
            case 'CANCELLED': case 'REJECTED': return 'badge-danger';
            case 'COMPLETED': return 'badge-secondary';
            case 'INVOICED': return 'badge-billed';
            case 'PAID': return 'badge-paid';
            default: return 'badge-info';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    formatCurrentTime(): string {
        return this.currentTime.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    formatCurrentDate(): string {
        return this.currentTime.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
    }

    getGreeting(): string {
        const hour = this.currentTime.getHours();
        if (hour < 12) return 'Good Morning';
        if (hour < 17) return 'Good Afternoon';
        return 'Good Evening';
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
            case 'confirm': obs = this.appointmentService.updateStatus(event.appointmentId, 'CONFIRMED'); break;
            case 'complete': obs = this.appointmentService.updateStatus(event.appointmentId, 'COMPLETED'); break;
            case 'cancel': obs = this.appointmentService.updateStatus(event.appointmentId, 'CANCELLED'); break;
            default: return;
        }
        obs.subscribe({
            next: () => { this.processingAction = false; this.selectedAppointment = null; this.loadData(); },
            error: () => { this.processingAction = false; }
        });
    }
}
