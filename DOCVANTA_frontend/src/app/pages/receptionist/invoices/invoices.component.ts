import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { InvoiceService } from '../../../services/invoice.service';
import { PaymentService } from '../../../services/payment.service';
import { MedicalActService } from '../../../services/medical-act.service';
import { PatientService } from '../../../services/patient.service';
import { AppointmentService } from '../../../services/appointment.service';
import { AuthService } from '../../../services/auth.service';
import { Invoice, InvoiceRequest, InvoiceLineRequest, MedicalAct, PaymentRequest } from '../../../models/billing.models';
import { Patient, Appointment } from '../../../models/auth.models';

interface NewLineItem {
    medicalActId: number;
    medicalActName: string;
    quantity: number;
    unitPrice: number;
    lineTotal: number;
}

@Component({
    selector: 'app-invoices',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './invoices.component.html',
    styleUrls: ['./invoices.component.css']
})
export class InvoicesComponent implements OnInit {
    // Data
    invoices: Invoice[] = [];
    filteredInvoices: Invoice[] = [];
    patients: Patient[] = [];
    medicalActs: MedicalAct[] = [];
    loading = true;
    errorMessage = '';
    successMessage = '';

    // Filters
    statusFilter = 'ALL';
    searchQuery = '';
    statusOptions = ['ALL', 'DRAFT', 'UNPAID', 'PAID', 'PARTIALLY_PAID', 'CANCELLED'];

    // Create Invoice Modal
    showCreateModal = false;
    createError = '';
    creating = false;
    newInvoice: {
        patientId: number;
        appointmentId: number;
        lines: NewLineItem[];
        discountAmount: number;
        discountReason: string;
        notes: string;
    } = { patientId: 0, appointmentId: 0, lines: [], discountAmount: 0, discountReason: '', notes: '' };

    // Patient search
    patientSearchQuery = '';
    filteredPatients: Patient[] = [];
    showPatientDropdown = false;
    selectedPatient: Patient | null = null;

    // Appointment selection
    patientAppointments: Appointment[] = [];
    loadingAppointments = false;

    // Line item form
    newLine: { medicalActId: number; quantity: number } = { medicalActId: 0, quantity: 1 };

    // Detail Modal
    showDetailModal = false;
    selectedInvoice: Invoice | null = null;
    detailLoading = false;

    // Payment sub-modal
    showPaymentModal = false;
    paymentRequest: PaymentRequest = { invoiceId: 0, amount: 0, paymentMethod: 'CASH', reference: '', notes: '' };
    paymentMethods = ['CASH', 'CARD', 'INSURANCE', 'BANK_TRANSFER', 'CHECK'];
    paymentError = '';
    submittingPayment = false;

    constructor(
        private invoiceService: InvoiceService,
        private paymentService: PaymentService,
        private medicalActService: MedicalActService,
        private patientService: PatientService,
        private appointmentService: AppointmentService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const total = 3;
        const checkDone = () => {
            completed++;
            if (completed >= total) {
                this.loading = false;
                this.cdr.detectChanges();
            }
        };

        this.invoiceService.getAll().subscribe({
            next: (data) => {
                this.invoices = (data || []).sort((a, b) =>
                    new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
                );
                this.applyFilters();
                checkDone();
            },
            error: () => { this.errorMessage = 'Failed to load invoices'; checkDone(); }
        });

        this.patientService.getAll().subscribe({
            next: (data) => { this.patients = data || []; checkDone(); },
            error: () => checkDone()
        });

        this.medicalActService.getAll().subscribe({
            next: (data) => { this.medicalActs = (data || []).filter(a => a.active); checkDone(); },
            error: () => checkDone()
        });
    }

    // ========== Filters ==========

    applyFilters(): void {
        let result = [...this.invoices];

        if (this.statusFilter !== 'ALL') {
            result = result.filter(i => i.status === this.statusFilter);
        }
        if (this.searchQuery.trim()) {
            const q = this.searchQuery.toLowerCase();
            result = result.filter(i =>
                i.patientName?.toLowerCase().includes(q) ||
                i.invoiceNumber?.toLowerCase().includes(q)
            );
        }

        this.filteredInvoices = result;
    }

    onFilterChange(): void {
        this.applyFilters();
    }

    // ========== Create Invoice Modal ==========

    openCreateModal(): void {
        this.showCreateModal = true;
        this.createError = '';
        this.newInvoice = { patientId: 0, appointmentId: 0, lines: [], discountAmount: 0, discountReason: '', notes: '' };
        this.newLine = { medicalActId: 0, quantity: 1 };
        this.patientSearchQuery = '';
        this.filteredPatients = [];
        this.showPatientDropdown = false;
        this.selectedPatient = null;
        this.patientAppointments = [];
    }

    closeCreateModal(): void {
        this.showCreateModal = false;
    }

    onPatientSearch(): void {
        const q = this.patientSearchQuery.toLowerCase().trim();
        if (q.length < 2) {
            this.filteredPatients = [];
            this.showPatientDropdown = false;
            return;
        }
        this.filteredPatients = this.patients.filter(p =>
            (p.firstName + ' ' + p.lastName).toLowerCase().includes(q) ||
            p.email?.toLowerCase().includes(q)
        ).slice(0, 10);
        this.showPatientDropdown = this.filteredPatients.length > 0;
    }

    selectPatient(patient: Patient): void {
        this.selectedPatient = patient;
        this.newInvoice.patientId = patient.userId;
        this.patientSearchQuery = patient.firstName + ' ' + patient.lastName;
        this.showPatientDropdown = false;
        this.newInvoice.appointmentId = 0;
        this.patientAppointments = [];
        this.loadPatientAppointments(patient.userId);
    }

    clearPatient(): void {
        this.selectedPatient = null;
        this.newInvoice.patientId = 0;
        this.newInvoice.appointmentId = 0;
        this.patientSearchQuery = '';
        this.patientAppointments = [];
    }

    private loadPatientAppointments(patientId: number): void {
        this.loadingAppointments = true;
        this.appointmentService.getByPatient(patientId).subscribe({
            next: (data) => {
                // Show completed/invoiced appointments that can be linked
                this.patientAppointments = (data || []).filter(a =>
                    a.status === 'COMPLETED' || a.status === 'INVOICED' || a.status === 'CONFIRMED'
                );
                this.loadingAppointments = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loadingAppointments = false;
                this.cdr.detectChanges();
            }
        });
    }

    formatAppointmentOption(apt: Appointment): string {
        const date = new Date(apt.datetime).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
        const ref = apt.referenceNumber || '';
        return `${ref} — ${date} — ${apt.practitionerName} (${apt.status})`;
    }

    getSelectedActPrice(): number {
        if (!this.newLine.medicalActId) return 0;
        const act = this.medicalActs.find(a => a.medicalActId === +this.newLine.medicalActId);
        return act ? act.basePrice : 0;
    }

    addLine(): void {
        if (!this.newLine.medicalActId || this.newLine.quantity < 1) {
            this.createError = 'Please select a medical act and set a valid quantity.';
            return;
        }
        const act = this.medicalActs.find(a => a.medicalActId === +this.newLine.medicalActId);
        if (!act) return;

        const line: NewLineItem = {
            medicalActId: act.medicalActId,
            medicalActName: act.name,
            quantity: this.newLine.quantity,
            unitPrice: act.basePrice,
            lineTotal: act.basePrice * this.newLine.quantity
        };
        this.newInvoice.lines.push(line);
        this.newLine = { medicalActId: 0, quantity: 1 };
        this.createError = '';
    }

    removeLine(index: number): void {
        this.newInvoice.lines.splice(index, 1);
    }

    getCreateTotal(): number {
        const subtotal = this.newInvoice.lines.reduce((sum, l) => sum + l.lineTotal, 0);
        return Math.max(0, subtotal - (this.newInvoice.discountAmount || 0));
    }

    getCreateSubtotal(): number {
        return this.newInvoice.lines.reduce((sum, l) => sum + l.lineTotal, 0);
    }

    saveAsDraft(): void {
        if (!this.newInvoice.patientId) {
            this.createError = 'Please select a patient.';
            return;
        }
        if (this.newInvoice.lines.length === 0) {
            this.createError = 'Please add at least one line item.';
            return;
        }

        this.creating = true;
        this.createError = '';

        const req: InvoiceRequest = {
            patientId: +this.newInvoice.patientId,
            appointmentId: this.newInvoice.appointmentId ? +this.newInvoice.appointmentId : undefined,
            lines: this.newInvoice.lines.map(l => ({
                medicalActId: l.medicalActId,
                quantity: l.quantity,
                unitPrice: l.unitPrice
            })),
            discountAmount: this.newInvoice.discountAmount || undefined,
            discountReason: this.newInvoice.discountReason || undefined,
            notes: this.newInvoice.notes || undefined
        };

        this.invoiceService.create(req).subscribe({
            next: () => {
                this.creating = false;
                this.showCreateModal = false;
                this.successMessage = 'Invoice created as draft successfully.';
                this.loadData();
                this.autoClearSuccess();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.creating = false;
                this.createError = err.error?.message || 'Failed to create invoice.';
                this.cdr.detectChanges();
            }
        });
    }

    // ========== Detail Modal ==========

    openDetailModal(invoice: Invoice): void {
        this.detailLoading = true;
        this.showDetailModal = true;
        this.selectedInvoice = null;

        this.invoiceService.getById(invoice.invoiceId).subscribe({
            next: (data) => {
                this.selectedInvoice = data;
                this.detailLoading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.selectedInvoice = invoice;
                this.detailLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    closeDetailModal(): void {
        this.showDetailModal = false;
        this.selectedInvoice = null;
        this.showPaymentModal = false;
    }

    finalizeInvoice(): void {
        if (!this.selectedInvoice) return;
        if (!confirm('Are you sure you want to finalize this invoice? This action cannot be undone.')) return;

        this.invoiceService.finalize(this.selectedInvoice.invoiceId).subscribe({
            next: (updated) => {
                this.selectedInvoice = updated;
                this.successMessage = 'Invoice finalized successfully.';
                this.loadData();
                this.autoClearSuccess();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to finalize invoice.';
                this.cdr.detectChanges();
            }
        });
    }

    cancelInvoice(): void {
        if (!this.selectedInvoice) return;
        if (!confirm('Are you sure you want to cancel this invoice?')) return;

        this.invoiceService.cancel(this.selectedInvoice.invoiceId).subscribe({
            next: (updated) => {
                this.selectedInvoice = updated;
                this.successMessage = 'Invoice cancelled successfully.';
                this.loadData();
                this.autoClearSuccess();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to cancel invoice.';
                this.cdr.detectChanges();
            }
        });
    }

    // ========== Payment Modal ==========

    openPaymentModal(): void {
        if (!this.selectedInvoice) return;
        this.showPaymentModal = true;
        this.paymentError = '';
        this.paymentRequest = {
            invoiceId: this.selectedInvoice.invoiceId,
            amount: this.selectedInvoice.remainingAmount,
            paymentMethod: 'CASH',
            reference: '',
            notes: ''
        };
    }

    closePaymentModal(): void {
        this.showPaymentModal = false;
    }

    submitPayment(): void {
        if (!this.paymentRequest.amount || this.paymentRequest.amount <= 0) {
            this.paymentError = 'Please enter a valid amount.';
            return;
        }
        if (this.selectedInvoice && this.paymentRequest.amount > this.selectedInvoice.remainingAmount) {
            this.paymentError = 'Amount cannot exceed the remaining balance.';
            return;
        }

        this.submittingPayment = true;
        this.paymentError = '';

        this.paymentService.record(this.paymentRequest).subscribe({
            next: () => {
                this.submittingPayment = false;
                this.showPaymentModal = false;
                this.successMessage = 'Payment recorded successfully.';
                this.autoClearSuccess();

                // Refresh detail
                if (this.selectedInvoice) {
                    this.invoiceService.getById(this.selectedInvoice.invoiceId).subscribe({
                        next: (updated) => {
                            this.selectedInvoice = updated;
                            this.loadData();
                            this.cdr.detectChanges();
                        },
                        error: () => {
                            this.loadData();
                            this.cdr.detectChanges();
                        }
                    });
                }
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.submittingPayment = false;
                this.paymentError = err.error?.message || 'Failed to record payment.';
                this.cdr.detectChanges();
            }
        });
    }

    // ========== Helpers ==========

    getStatusClass(status: string): string {
        switch (status) {
            case 'DRAFT': return 'badge-draft';
            case 'UNPAID': return 'badge-finalized';
            case 'PAID': return 'badge-paid';
            case 'PARTIALLY_PAID': return 'badge-partial';
            case 'CANCELLED': return 'badge-cancelled';
            default: return 'badge-draft';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    formatCurrency(amount: number): string {
        return (amount || 0).toFixed(2) + ' MAD';
    }

    formatPaymentMethod(method: string): string {
        switch (method) {
            case 'CASH': return 'Cash';
            case 'CARD': return 'Card';
            case 'INSURANCE': return 'Insurance';
            case 'BANK_TRANSFER': return 'Bank Transfer';
            case 'CHECK': return 'Check';
            default: return method;
        }
    }

    private autoClearSuccess(): void {
        setTimeout(() => {
            this.successMessage = '';
            this.cdr.detectChanges();
        }, 4000);
    }
}
