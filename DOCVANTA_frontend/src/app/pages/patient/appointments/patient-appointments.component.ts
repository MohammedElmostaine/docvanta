import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AppointmentService } from '../../../services/appointment.service';
import { PractitionerService } from '../../../services/practitioner.service';
import { TimeSlot } from '../../../models/billing.models';

@Component({
    selector: 'app-patient-appointments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './patient-appointments.component.html',
    styleUrls: ['./patient-appointments.component.css']
})
export class PatientAppointmentsComponent implements OnInit {
    user: any;
    appointments: any[] = [];
    filteredAppointments: any[] = [];
    practitioners: any[] = [];
    loading = true;
    showRequestModal = false;
    showCancelModal = false;
    selectedAppointment: any = null;
    submitting = false;
    successMessage = '';
    errorMessage = '';

    statusFilter = 'ALL';
    periodFilter = 'ALL';
    searchTerm = '';

    // Request form state
    selectedPractitionerId: number | null = null;
    selectedDate = '';
    selectedSlot: TimeSlot | null = null;
    reason = '';
    availableSlots: TimeSlot[] = [];
    loadingSlots = false;
    slotsError = '';

    // Min date for date picker (today)
    minDate = '';

    constructor(
        private authService: AuthService,
        private appointmentService: AppointmentService,
        private practitionerService: PractitionerService,
        private cdr: ChangeDetectorRef
    ) {
        this.user = this.authService.getUser();
        const today = new Date();
        this.minDate = today.toISOString().split('T')[0];
    }

    ngOnInit(): void {
        this.loadAppointments();
        this.loadPractitioners();
    }

    private loadAppointments(): void {
        this.loading = true;
        const userId = this.user?.userId;
        if (!userId) {
            this.loading = false;
            return;
        }

        this.appointmentService.getByPatient(userId).subscribe({
            next: (data) => {
                this.appointments = (data || []).sort(
                    (a: any, b: any) => new Date(b.datetime).getTime() - new Date(a.datetime).getTime()
                );
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.appointments = [];
                this.filteredAppointments = [];
                this.loading = false;
                this.errorMessage = 'Failed to load appointments.';
                this.cdr.detectChanges();
            }
        });
    }

    private loadPractitioners(): void {
        this.practitionerService.getAll().subscribe({
            next: (data) => { this.practitioners = data || []; },
            error: () => { this.practitioners = []; }
        });
    }

    applyFilters(): void {
        let result = [...this.appointments];
        const now = new Date();

        if (this.statusFilter !== 'ALL') {
            result = result.filter(a => a.status === this.statusFilter);
        }

        if (this.periodFilter === 'UPCOMING') {
            result = result.filter(a => new Date(a.datetime) >= now);
        } else if (this.periodFilter === 'PAST') {
            result = result.filter(a => new Date(a.datetime) < now);
        }

        if (this.searchTerm.trim()) {
            const term = this.searchTerm.toLowerCase();
            result = result.filter(a =>
                (a.practitionerName || '').toLowerCase().includes(term) ||
                (a.status || '').toLowerCase().includes(term)
            );
        }

        this.filteredAppointments = result;
    }

    openRequestModal(): void {
        this.selectedPractitionerId = null;
        this.selectedDate = '';
        this.selectedSlot = null;
        this.reason = '';
        this.availableSlots = [];
        this.slotsError = '';
        this.showRequestModal = true;
        this.errorMessage = '';
    }

    closeRequestModal(): void {
        this.showRequestModal = false;
    }

    onPractitionerChange(): void {
        this.selectedSlot = null;
        this.availableSlots = [];
        this.slotsError = '';
        if (this.selectedPractitionerId && this.selectedDate) {
            this.loadSlots();
        }
    }

    onDateChange(): void {
        this.selectedSlot = null;
        this.availableSlots = [];
        this.slotsError = '';
        if (this.selectedPractitionerId && this.selectedDate) {
            this.loadSlots();
        }
    }

    private loadSlots(): void {
        if (!this.selectedPractitionerId || !this.selectedDate) return;
        this.loadingSlots = true;
        this.slotsError = '';

        this.appointmentService.getAvailableSlots(this.selectedPractitionerId, this.selectedDate).subscribe({
            next: (slots) => {
                this.availableSlots = slots || [];
                // Filter out past slots if selected date is today
                const now = new Date();
                const today = now.toISOString().split('T')[0];
                if (this.selectedDate === today) {
                    this.availableSlots = this.availableSlots.map(slot => {
                        const slotTime = new Date(slot.datetime);
                        return slotTime <= now ? { ...slot, available: false } : slot;
                    });
                }
                this.loadingSlots = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loadingSlots = false;
                this.slotsError = 'Could not load available slots. Please try again.';
                this.cdr.detectChanges();
            }
        });
    }

    selectSlot(slot: TimeSlot): void {
        if (!slot.available) return;
        this.selectedSlot = slot;
    }

    get hasAvailableSlots(): boolean {
        return this.availableSlots.some(s => s.available);
    }

    getSelectedPractitionerName(): string {
        const doc = this.practitioners.find(p => p.userId === this.selectedPractitionerId);
        return doc ? `Dr. ${doc.firstName} ${doc.lastName}` : '';
    }

    formatSlotTime(time: string): string {
        if (!time) return '';
        const parts = time.split(':');
        if (parts.length >= 2) {
            const h = parseInt(parts[0], 10);
            const m = parts[1];
            return `${h.toString().padStart(2, '0')}:${m}`;
        }
        return time;
    }

    formatSelectedDate(): string {
        if (!this.selectedDate) return '';
        const d = new Date(this.selectedDate + 'T00:00:00');
        return d.toLocaleDateString('en-US', { weekday: 'long', month: 'long', day: 'numeric', year: 'numeric' });
    }

    submitAppointment(): void {
        if (!this.selectedPractitionerId || !this.selectedSlot) {
            this.errorMessage = 'Please select a practitioner and a time slot.';
            return;
        }

        this.submitting = true;
        this.errorMessage = '';

        const request: any = {
            practitionerId: this.selectedPractitionerId,
            patientId: this.user.userId,
            datetime: this.selectedSlot.datetime
        };
        if (this.reason.trim()) {
            request.reason = this.reason.trim();
        }

        this.appointmentService.create(request).subscribe({
            next: () => {
                this.submitting = false;
                this.showRequestModal = false;
                this.successMessage = 'Appointment requested successfully!';
                this.loadAppointments();
                this.cdr.detectChanges();
                setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 4000);
            },
            error: (err) => {
                this.submitting = false;
                this.errorMessage = err?.error?.message || 'Failed to request appointment.';
                this.cdr.detectChanges();
            }
        });
    }

    openCancelModal(appointment: any): void {
        this.selectedAppointment = appointment;
        this.showCancelModal = true;
    }

    closeCancelModal(): void {
        this.showCancelModal = false;
        this.selectedAppointment = null;
    }

    confirmCancel(): void {
        if (!this.selectedAppointment) return;

        this.submitting = true;
        this.appointmentService.updateStatus(this.selectedAppointment.appointmentId, 'CANCELLED').subscribe({
            next: () => {
                this.submitting = false;
                this.showCancelModal = false;
                this.selectedAppointment = null;
                this.successMessage = 'Appointment cancelled successfully.';
                this.loadAppointments();
                this.cdr.detectChanges();
                setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 4000);
            },
            error: () => {
                this.submitting = false;
                this.errorMessage = 'Failed to cancel appointment.';
                this.cdr.detectChanges();
            }
        });
    }

    isUpcoming(dateStr: string): boolean {
        return new Date(dateStr) >= new Date();
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

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { weekday: 'long', month: 'short', day: 'numeric', year: 'numeric' });
    }

    formatTime(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' });
    }

    getDay(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).getDate().toString();
    }

    getMonth(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short' });
    }

    getYear(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).getFullYear().toString();
    }

    getWeekday(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { weekday: 'short' });
    }
}
