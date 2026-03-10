import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Appointment } from '../../models/auth.models';

export type DashboardRole = 'PRACTITIONER' | 'PATIENT' | 'STAFF' | 'RECEPTIONIST' | 'ADMIN'
    | 'SYSTEM_ADMINISTRATOR' | 'CLINICAL_ASSISTANT' | 'NURSE' | 'TECHNICIAN' | 'PHARMACIST';

const STAFF_ROLES: DashboardRole[] = ['STAFF', 'CLINICAL_ASSISTANT', 'NURSE', 'TECHNICIAN', 'PHARMACIST'];

export interface AppointmentAction {
    type: 'confirm' | 'reject' | 'complete' | 'cancel';
    appointmentId: number;
}

@Component({
    selector: 'app-appointment-detail-modal',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './appointment-detail-modal.component.html',
    styleUrls: ['./appointment-detail-modal.component.css']
})
export class AppointmentDetailModalComponent {
    @Input() appointment: Appointment | null = null;
    @Input() role: DashboardRole = 'PATIENT';
    @Input() processing = false;
    @Output() close = new EventEmitter<void>();
    @Output() action = new EventEmitter<AppointmentAction>();

    onOverlayClick(): void {
        this.close.emit();
    }

    onClose(): void {
        this.close.emit();
    }

    emitAction(type: AppointmentAction['type']): void {
        if (this.appointment) {
            this.action.emit({ type, appointmentId: this.appointment.appointmentId });
        }
    }

    // ── Visibility rules per role ──

    private get isStaffRole(): boolean {
        return STAFF_ROLES.includes(this.role);
    }

    private get isAdminRole(): boolean {
        return this.role === 'ADMIN' || this.role === 'SYSTEM_ADMINISTRATOR';
    }

    get canConfirm(): boolean {
        if (!this.appointment) return false;
        const s = this.appointment.status;
        return s === 'PENDING' && (this.role === 'PRACTITIONER' || this.isStaffRole || this.role === 'RECEPTIONIST' || this.isAdminRole);
    }

    get canReject(): boolean {
        if (!this.appointment) return false;
        return this.appointment.status === 'PENDING' && (this.role === 'PRACTITIONER' || this.isAdminRole);
    }

    get canComplete(): boolean {
        if (!this.appointment) return false;
        return this.appointment.status === 'CONFIRMED' && (this.role === 'PRACTITIONER' || this.isStaffRole || this.isAdminRole);
    }

    get canCancel(): boolean {
        if (!this.appointment) return false;
        const nonCancellable = ['COMPLETED', 'CANCELLED', 'REJECTED', 'INVOICED', 'PAID'];
        if (nonCancellable.includes(this.appointment.status)) return false;
        return this.role === 'PRACTITIONER' || this.role === 'PATIENT' || this.isStaffRole || this.role === 'RECEPTIONIST' || this.isAdminRole;
    }

    get hasActions(): boolean {
        return this.canConfirm || this.canReject || this.canComplete || this.canCancel;
    }

    getStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PENDING': return 'status-pending';
            case 'CONFIRMED': return 'status-confirmed';
            case 'COMPLETED': return 'status-completed';
            case 'INVOICED': return 'status-invoiced';
            case 'PAID': return 'status-paid';
            case 'CANCELLED': return 'status-cancelled';
            case 'REJECTED': return 'status-rejected';
            default: return 'status-default';
        }
    }

    getPaymentStatusClass(status: string): string {
        switch (status?.toUpperCase()) {
            case 'PAID': return 'payment-paid';
            case 'PARTIALLY_PAID': return 'payment-partial';
            case 'UNPAID': return 'payment-unpaid';
            default: return 'payment-unpaid';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('en-US', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
        });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    formatPrice(val?: number): string {
        if (val == null) return '-';
        return val.toFixed(2) + ' MAD';
    }
}
