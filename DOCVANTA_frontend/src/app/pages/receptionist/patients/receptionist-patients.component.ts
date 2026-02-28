import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PatientService } from '../../../services/patient.service';
import { InvoiceService } from '../../../services/invoice.service';
import { Patient } from '../../../models/auth.models';
import { Invoice } from '../../../models/billing.models';

@Component({
    selector: 'app-receptionist-patients',
    standalone: true,
    imports: [CommonModule, FormsModule, RouterLink],
    templateUrl: './receptionist-patients.component.html',
    styleUrls: ['./receptionist-patients.component.css']
})
export class ReceptionistPatientsComponent implements OnInit {
    patients: Patient[] = [];
    filteredPatients: Patient[] = [];
    searchQuery: string = '';

    // Invoice modal
    showInvoiceModal = false;
    selectedPatient: Patient | null = null;
    patientInvoices: Invoice[] = [];
    invoicesLoading = false;

    loading = true;
    error: string | null = null;

    constructor(
        private patientService: PatientService,
        private invoiceService: InvoiceService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadPatients();
    }

    loadPatients(): void {
        this.loading = true;
        this.error = null;

        this.patientService.getAll().subscribe({
            next: (data) => {
                this.patients = data || [];
                this.applySearch();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.error = 'Failed to load patients. Please try again.';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applySearch(): void {
        const query = this.searchQuery.trim().toLowerCase();
        if (!query) {
            this.filteredPatients = [...this.patients];
        } else {
            this.filteredPatients = this.patients.filter(p => {
                const fullName = `${p.firstName} ${p.lastName}`.toLowerCase();
                return fullName.includes(query)
                    || (p.email && p.email.toLowerCase().includes(query))
                    || (p.phone && p.phone.includes(query));
            });
        }
        this.cdr.detectChanges();
    }

    onSearchChange(): void {
        this.applySearch();
    }

    viewInvoices(patient: Patient): void {
        this.selectedPatient = patient;
        this.patientInvoices = [];
        this.showInvoiceModal = true;
        this.invoicesLoading = true;
        this.cdr.detectChanges();

        this.invoiceService.getByPatient(patient.userId).subscribe({
            next: (invoices) => {
                this.patientInvoices = (invoices || []).sort(
                    (a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime()
                );
                this.invoicesLoading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.patientInvoices = [];
                this.invoicesLoading = false;
                this.cdr.detectChanges();
            }
        });
    }

    closeModal(): void {
        this.showInvoiceModal = false;
        this.selectedPatient = null;
        this.patientInvoices = [];
        this.cdr.detectChanges();
    }

    onOverlayClick(event: MouseEvent): void {
        if ((event.target as HTMLElement).classList.contains('modal-overlay')) {
            this.closeModal();
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', {
            day: '2-digit',
            month: 'short',
            year: 'numeric'
        });
    }

    formatCurrency(amount: number): string {
        return new Intl.NumberFormat('fr-FR', { style: 'currency', currency: 'MAD' }).format(amount || 0);
    }

    getInvoiceStatusClass(status: string): string {
        switch (status) {
            case 'PAID': return 'badge-paid';
            case 'FINALIZED': return 'badge-finalized';
            case 'PARTIALLY_PAID': return 'badge-partial';
            case 'DRAFT': return 'badge-draft';
            case 'CANCELLED': return 'badge-cancelled';
            case 'REFUNDED': return 'badge-refunded';
            default: return 'badge-default';
        }
    }

    get selectedPatientName(): string {
        if (!this.selectedPatient) return '';
        return `${this.selectedPatient.firstName} ${this.selectedPatient.lastName}`;
    }
}
