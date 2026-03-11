import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { MedicalActService } from '../../../services/medical-act.service';
import { Appointment } from '../../../models/auth.models';
import { PerformedAct, PerformedActRequest, MedicalAct } from '../../../models/billing.models';

@Component({
    selector: 'app-practitioner-appointments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './practitioner-appointments.component.html',
    styleUrls: ['./practitioner-appointments.component.css']
})
export class PractitionerAppointmentsComponent implements OnInit {
    user: any;
    appointments: Appointment[] = [];
    filteredAppointments: Appointment[] = [];
    loading = true;
    errorMessage = '';
    successMessage = '';

    statusFilter = 'ALL';
    periodFilter = 'ALL';
    searchQuery = '';

    // Performed acts
    expandedAppointmentId: number | null = null;
    performedActs: PerformedAct[] = [];
    actsLoading = false;
    medicalActs: MedicalAct[] = [];
    selectedActId: number | null = null;
    actQuantity = 1;
    actNotes = '';
    addingAct = false;

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private medicalActService: MedicalActService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadAppointments();
        this.loadMedicalActs();
    }

    loadMedicalActs(): void {
        this.medicalActService.getAll().subscribe({
            next: (acts) => this.medicalActs = (acts || []).filter(a => a.active),
            error: () => {}
        });
    }

    loadAppointments(): void {
        this.loading = true;
        const practitionerId = this.user?.userId;
        if (!practitionerId) { this.loading = false; return; }

        this.appointmentService.getByPractitioner(practitionerId).subscribe({
            next: (data) => {
                this.appointments = (data || []).sort((a, b) => new Date(b.datetime).getTime() - new Date(a.datetime).getTime());
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load appointments';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.appointments];
        if (this.statusFilter !== 'ALL') {
            result = result.filter(a => a.status === this.statusFilter);
        }
        if (this.periodFilter === 'UPCOMING') {
            result = result.filter(a => new Date(a.datetime) >= new Date());
        } else if (this.periodFilter === 'PAST') {
            result = result.filter(a => new Date(a.datetime) < new Date());
        } else if (this.periodFilter === 'TODAY') {
            const today = new Date().toISOString().split('T')[0];
            result = result.filter(a => a.datetime?.startsWith(today));
        }
        if (this.searchQuery) {
            const q = this.searchQuery.toLowerCase();
            result = result.filter(a => (a.patientName || '').toLowerCase().includes(q) || (a.reason || '').toLowerCase().includes(q));
        }
        this.filteredAppointments = result;
    }

    // ── Performed Acts ──
    togglePerformedActs(apt: Appointment): void {
        if (this.expandedAppointmentId === apt.appointmentId) {
            this.expandedAppointmentId = null;
            this.performedActs = [];
        } else {
            this.expandedAppointmentId = apt.appointmentId;
            this.loadPerformedActs(apt.appointmentId);
        }
    }

    loadPerformedActs(appointmentId: number): void {
        this.actsLoading = true;
        this.appointmentService.getPerformedActs(appointmentId).subscribe({
            next: (acts) => { this.performedActs = acts || []; this.actsLoading = false; this.cdr.detectChanges(); },
            error: () => { this.performedActs = []; this.actsLoading = false; this.cdr.detectChanges(); }
        });
    }

    addPerformedAct(): void {
        if (!this.selectedActId || !this.expandedAppointmentId) return;
        this.addingAct = true;
        const req: PerformedActRequest = {
            medicalActId: this.selectedActId,
            quantity: this.actQuantity,
            notes: this.actNotes || undefined
        };
        this.appointmentService.addPerformedAct(this.expandedAppointmentId, req).subscribe({
            next: () => {
                this.showSuccess('Performed act added');
                this.selectedActId = null;
                this.actQuantity = 1;
                this.actNotes = '';
                this.addingAct = false;
                this.loadPerformedActs(this.expandedAppointmentId!);
                this.loadAppointments();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to add act';
                this.addingAct = false;
                this.cdr.detectChanges();
            }
        });
    }

    removePerformedAct(actId: number): void {
        if (!this.expandedAppointmentId || !confirm('Remove this performed act?')) return;
        this.appointmentService.removePerformedAct(this.expandedAppointmentId, actId).subscribe({
            next: () => {
                this.showSuccess('Performed act removed');
                this.loadPerformedActs(this.expandedAppointmentId!);
                this.loadAppointments();
            },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to remove act'; this.cdr.detectChanges(); }
        });
    }

    canAddActs(a: Appointment): boolean {
        return a.status === 'CONFIRMED' || a.status === 'COMPLETED';
    }

    canRemoveActs(a: Appointment): boolean {
        return a.status === 'CONFIRMED' || a.status === 'COMPLETED';
    }

    getActTotal(): number {
        return this.performedActs.reduce((sum, a) => sum + a.totalPrice, 0);
    }

    // ── Status Actions ──
    confirmAppointment(id: number): void {
        this.appointmentService.confirm(id).subscribe({
            next: () => { this.showSuccess('Appointment confirmed'); this.loadAppointments(); },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to confirm'; this.cdr.detectChanges(); }
        });
    }

    rejectAppointment(id: number): void {
        if (confirm('Reject this appointment?')) {
            this.appointmentService.reject(id).subscribe({
                next: () => { this.showSuccess('Appointment rejected'); this.loadAppointments(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to reject'; this.cdr.detectChanges(); }
            });
        }
    }

    completeAppointment(id: number): void {
        this.appointmentService.complete(id).subscribe({
            next: () => { this.showSuccess('Appointment completed'); this.loadAppointments(); },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to complete'; this.cdr.detectChanges(); }
        });
    }

    cancelAppointment(id: number): void {
        if (confirm('Cancel this appointment?')) {
            this.appointmentService.cancel(id).subscribe({
                next: () => { this.showSuccess('Appointment cancelled'); this.loadAppointments(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to cancel'; this.cdr.detectChanges(); }
            });
        }
    }

    private showSuccess(msg: string): void {
        this.successMessage = msg;
        setTimeout(() => this.successMessage = '', 3000);
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    formatPrice(val?: number): string {
        if (val == null) return '-';
        return val.toFixed(2) + ' MAD';
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

    canConfirm(a: Appointment): boolean { return a.status === 'PENDING'; }
    canReject(a: Appointment): boolean { return a.status === 'PENDING'; }
    canComplete(a: Appointment): boolean { return a.status === 'CONFIRMED'; }
    canCancel(a: Appointment): boolean { return !['COMPLETED', 'CANCELLED', 'REJECTED', 'INVOICED', 'PAID'].includes(a.status); }
}
