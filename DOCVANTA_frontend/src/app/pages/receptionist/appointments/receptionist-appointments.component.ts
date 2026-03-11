import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AppointmentService } from '../../../services/appointment.service';
import { InvoiceService } from '../../../services/invoice.service';
import { Appointment } from '../../../models/auth.models';

@Component({
    selector: 'app-receptionist-appointments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './receptionist-appointments.component.html',
    styleUrls: ['./receptionist-appointments.component.css']
})
export class ReceptionistAppointmentsComponent implements OnInit {
    appointments: Appointment[] = [];
    filteredAppointments: Appointment[] = [];

    selectedDate: string = '';
    selectedStatus: string = 'ALL';
    statuses: string[] = ['ALL', 'PENDING', 'CONFIRMED', 'COMPLETED', 'INVOICED', 'PAID', 'CANCELLED', 'REJECTED'];

    loading = true;
    error: string | null = null;
    successMessage: string | null = null;

    constructor(
        private appointmentService: AppointmentService,
        private invoiceService: InvoiceService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.selectedDate = new Date().toISOString().split('T')[0];
        this.loadAppointments();
    }

    loadAppointments(): void {
        this.loading = true;
        this.error = null;
        this.successMessage = null;

        this.appointmentService.getAll().subscribe({
            next: (data) => {
                this.appointments = (data || []).sort(
                    (a, b) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime()
                );
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.error = 'Failed to load appointments. Please try again.';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.appointments];

        if (this.selectedDate) {
            result = result.filter(a => a.datetime?.startsWith(this.selectedDate));
        }

        if (this.selectedStatus !== 'ALL') {
            result = result.filter(a => a.status === this.selectedStatus);
        }

        this.filteredAppointments = result;
        this.cdr.detectChanges();
    }

    onDateChange(): void {
        this.applyFilters();
    }

    onStatusChange(): void {
        this.applyFilters();
    }

    confirmAppointment(id: number): void {
        this.successMessage = null;
        this.error = null;

        this.appointmentService.confirm(id).subscribe({
            next: (updated) => {
                const idx = this.appointments.findIndex(a => a.appointmentId === id);
                if (idx !== -1) {
                    this.appointments[idx] = updated;
                }
                this.applyFilters();
                this.successMessage = 'Appointment confirmed successfully.';
                this.cdr.detectChanges();
                this.clearMessageAfterDelay();
            },
            error: () => {
                this.error = 'Failed to confirm appointment.';
                this.cdr.detectChanges();
            }
        });
    }

    generateInvoice(appointmentId: number): void {
        this.successMessage = null;
        this.error = null;

        this.invoiceService.generateFromAppointment(appointmentId).subscribe({
            next: () => {
                this.successMessage = 'Invoice generated successfully.';
                this.loadAppointments();
                this.cdr.detectChanges();
                this.clearMessageAfterDelay();
            },
            error: (err) => {
                this.error = err.error?.message || 'Failed to generate invoice.';
                this.cdr.detectChanges();
            }
        });
    }

    cancelAppointment(id: number): void {
        this.successMessage = null;
        this.error = null;

        this.appointmentService.cancel(id).subscribe({
            next: (updated) => {
                const idx = this.appointments.findIndex(a => a.appointmentId === id);
                if (idx !== -1) {
                    this.appointments[idx] = updated;
                }
                this.applyFilters();
                this.successMessage = 'Appointment cancelled successfully.';
                this.cdr.detectChanges();
                this.clearMessageAfterDelay();
            },
            error: () => {
                this.error = 'Failed to cancel appointment.';
                this.cdr.detectChanges();
            }
        });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'PENDING': return 'badge-pending';
            case 'INVOICED': return 'badge-invoiced';
            case 'CONFIRMED': return 'badge-confirmed';
            case 'COMPLETED': return 'badge-completed';
            case 'CANCELLED': return 'badge-cancelled';
            default: return 'badge-default';
        }
    }

    getStatusCount(status: string): number {
        const source = this.selectedDate
            ? this.appointments.filter(a => a.datetime?.startsWith(this.selectedDate))
            : this.appointments;
        if (status === 'ALL') return source.length;
        return source.filter(a => a.status === status).length;
    }

    private clearMessageAfterDelay(): void {
        setTimeout(() => {
            this.successMessage = null;
            this.cdr.detectChanges();
        }, 3000);
    }
}
