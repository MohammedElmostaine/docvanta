import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { InvoiceService } from '../../../services/invoice.service';
import { Appointment } from '../../../models/auth.models';
import { Invoice } from '../../../models/billing.models';
import { AppointmentDetailModalComponent, AppointmentAction } from '../../../components/appointment-detail-modal/appointment-detail-modal.component';

@Component({
    selector: 'app-receptionist-dashboard',
    standalone: true,
    imports: [CommonModule, RouterLink, FormsModule, AppointmentDetailModalComponent],
    templateUrl: './receptionist-dashboard.component.html',
    styleUrls: ['./receptionist-dashboard.component.css']
})
export class ReceptionistDashboardComponent implements OnInit {
    user: { userId: number; username: string; role: string } | null = null;

    todayAppointments: Appointment[] = [];
    unpaidInvoices: Invoice[] = [];

    stats = {
        todayAppointments: 0,
        unpaidInvoices: 0,
        todayRevenue: 0
    };

    loading = true;
    error: string | null = null;
    selectedAppointment: Appointment | null = null;
    processingAction = false;

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private invoiceService: InvoiceService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        this.error = null;
        let completed = 0;
        const total = 2;
        const checkDone = () => {
            completed++;
            if (completed >= total) {
                this.loading = false;
                this.cdr.detectChanges();
            }
        };

        // Load appointments
        this.appointmentService.getAll().subscribe({
            next: (appointments) => {
                const all = appointments || [];
                const today = new Date().toISOString().split('T')[0];
                const todayAppts = all
                    .filter(a => a.datetime?.startsWith(today))
                    .sort((a, b) => new Date(a.datetime).getTime() - new Date(b.datetime).getTime());
                this.stats.todayAppointments = todayAppts.length;
                this.todayAppointments = todayAppts.slice(0, 8);
                checkDone();
            },
            error: () => checkDone()
        });

        // Load invoices
        this.invoiceService.getAll().subscribe({
            next: (invoices) => {
                const all = invoices || [];
                const today = new Date().toISOString().split('T')[0];

                // Unpaid invoices (not PAID and not CANCELLED)
                this.unpaidInvoices = all
                    .filter(inv => inv.status !== 'PAID' && inv.status !== 'CANCELLED' && inv.status !== 'REFUNDED')
                    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());
                this.stats.unpaidInvoices = this.unpaidInvoices.length;

                // Today's revenue: sum of paidAmount for invoices paid today
                this.stats.todayRevenue = all
                    .filter(inv => inv.status === 'PAID' && inv.createdAt?.startsWith(today))
                    .reduce((sum, inv) => sum + (inv.paidAmount || 0), 0);

                checkDone();
            },
            error: () => checkDone()
        });

    }

    checkIn(appointment: Appointment): void {
        this.appointmentService.confirm(appointment.appointmentId).subscribe({
            next: (updated) => {
                const idx = this.todayAppointments.findIndex(a => a.appointmentId === appointment.appointmentId);
                if (idx !== -1) {
                    this.todayAppointments[idx] = updated;
                    this.cdr.detectChanges();
                }
            },
            error: () => {
                // silently fail — user can retry
            }
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'CONFIRMED': return 'badge-success';
            case 'PENDING': return 'badge-warning';
            case 'CANCELLED': return 'badge-danger';
            case 'COMPLETED': return 'badge-info';
            case 'CHECKED_IN': return 'badge-primary';
            default: return 'badge-secondary';
        }
    }

    getInvoiceStatusClass(status: string): string {
        switch (status) {
            case 'PAID': return 'badge-success';
            case 'FINALIZED': return 'badge-info';
            case 'PARTIALLY_PAID': return 'badge-warning';
            case 'DRAFT': return 'badge-secondary';
            case 'CANCELLED': return 'badge-danger';
            case 'REFUNDED': return 'badge-danger';
            default: return 'badge-secondary';
        }
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MAD' }).format(amount || 0);
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
            case 'cancel': obs = this.appointmentService.cancel(event.appointmentId); break;
            default: return;
        }
        obs.subscribe({
            next: () => { this.processingAction = false; this.selectedAppointment = null; this.loadData(); },
            error: () => { this.processingAction = false; }
        });
    }
}
