import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { InvoiceService } from '../../../services/invoice.service';
import { PaymentService } from '../../../services/payment.service';
import { Invoice, PaymentRecord, PaymentRequest } from '../../../models/billing.models';

@Component({
    selector: 'app-payments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './payments.component.html',
    styleUrls: ['./payments.component.css']
})
export class PaymentsComponent implements OnInit {
    // Data
    unpaidInvoices: Invoice[] = [];
    allPayments: PaymentRecord[] = [];
    filteredPayments: PaymentRecord[] = [];
    loading = true;
    error: string | null = null;
    successMessage: string | null = null;

    // Tab
    activeTab: 'unpaid' | 'history' = 'unpaid';

    // Payment modal
    showPaymentModal = false;
    selectedInvoice: Invoice | null = null;
    paymentAmount = 0;
    paymentMethod = 'CASH';
    paymentReference = '';
    paymentNotes = '';
    submitting = false;
    paymentError = '';

    // History filter
    selectedMethod = 'ALL';
    selectedDate = '';
    methods = ['ALL', 'CASH', 'CARD', 'INSURANCE', 'BANK_TRANSFER', 'CHECK'];

    // Summary
    totalToday = 0;
    cashTotal = 0;
    cardTotal = 0;

    constructor(
        private invoiceService: InvoiceService,
        private paymentService: PaymentService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        this.error = null;

        this.invoiceService.getAll().subscribe({
            next: (invoices) => {
                const all = invoices || [];

                // Unpaid invoices: UNPAID or PARTIALLY_PAID
                this.unpaidInvoices = all
                    .filter(i => i.status === 'UNPAID' || i.status === 'PARTIALLY_PAID')
                    .sort((a, b) => new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime());

                // Payment history from all invoices
                this.allPayments = [];
                for (const invoice of all) {
                    if (invoice.payments?.length) {
                        for (const payment of invoice.payments) {
                            this.allPayments.push({
                                ...payment,
                                patientName: payment.patientName || invoice.patientName
                            });
                        }
                    }
                }
                this.allPayments.sort(
                    (a, b) => new Date(b.paymentDate).getTime() - new Date(a.paymentDate).getTime()
                );

                this.computeSummary();
                this.applyFilter();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.error = 'Failed to load data. Please try again.';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    computeSummary(): void {
        const today = new Date().toISOString().split('T')[0];
        const todayPayments = this.allPayments.filter(p => p.paymentDate?.startsWith(today));
        this.totalToday = todayPayments.reduce((sum, p) => sum + (p.amount || 0), 0);
        this.cashTotal = todayPayments.filter(p => p.paymentMethod === 'CASH').reduce((sum, p) => sum + (p.amount || 0), 0);
        this.cardTotal = todayPayments.filter(p => p.paymentMethod === 'CARD').reduce((sum, p) => sum + (p.amount || 0), 0);
    }

    applyFilter(): void {
        let result = [...this.allPayments];

        if (this.selectedMethod !== 'ALL') {
            result = result.filter(p => p.paymentMethod === this.selectedMethod);
        }

        if (this.selectedDate) {
            result = result.filter(p => p.paymentDate?.startsWith(this.selectedDate));
        }

        this.filteredPayments = result;
    }

    onFilterChange(): void {
        this.applyFilter();
    }

    clearDateFilter(): void {
        this.selectedDate = '';
        this.applyFilter();
    }

    // ── Payment Modal ──

    openPaymentModal(invoice: Invoice): void {
        this.selectedInvoice = invoice;
        this.paymentAmount = invoice.remainingAmount;
        this.paymentMethod = 'CASH';
        this.paymentReference = '';
        this.paymentNotes = '';
        this.paymentError = '';
        this.showPaymentModal = true;
    }

    closePaymentModal(): void {
        this.showPaymentModal = false;
        this.selectedInvoice = null;
    }

    payFull(): void {
        if (this.selectedInvoice) {
            this.paymentAmount = this.selectedInvoice.remainingAmount;
        }
    }

    submitPayment(): void {
        if (!this.selectedInvoice) return;
        if (!this.paymentAmount || this.paymentAmount <= 0) {
            this.paymentError = 'Enter a valid amount.';
            return;
        }
        if (this.paymentAmount > this.selectedInvoice.remainingAmount) {
            this.paymentError = 'Amount exceeds remaining balance.';
            return;
        }

        this.submitting = true;
        this.paymentError = '';

        const req: PaymentRequest = {
            invoiceId: this.selectedInvoice.invoiceId,
            amount: this.paymentAmount,
            paymentMethod: this.paymentMethod,
            reference: this.paymentReference || undefined,
            notes: this.paymentNotes || undefined
        };

        this.paymentService.record(req).subscribe({
            next: () => {
                this.submitting = false;
                this.showPaymentModal = false;
                this.successMessage = `Payment of ${this.formatCurrency(this.paymentAmount)} recorded successfully.`;
                this.loadData();
                this.cdr.detectChanges();
                setTimeout(() => { this.successMessage = null; this.cdr.detectChanges(); }, 4000);
            },
            error: (err) => {
                this.submitting = false;
                this.paymentError = err.error?.message || 'Failed to record payment.';
                this.cdr.detectChanges();
            }
        });
    }

    // ── Helpers ──

    getProgressPercent(invoice: Invoice): number {
        if (!invoice.totalAmount) return 0;
        return Math.min(100, Math.round((invoice.paidAmount / invoice.totalAmount) * 100));
    }

    formatCurrency(amount: number): string {
        return (amount || 0).toFixed(2) + ' MAD';
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    formatDateTime(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', {
            day: '2-digit', month: 'short', year: 'numeric', hour: '2-digit', minute: '2-digit'
        });
    }

    getStatusClass(status: string): string {
        switch (status) {
            case 'UNPAID': return 'badge-unpaid';
            case 'PARTIALLY_PAID': return 'badge-partial';
            case 'PAID': return 'badge-paid';
            default: return 'badge-default';
        }
    }

    getMethodClass(method: string): string {
        switch (method) {
            case 'CASH': return 'badge-cash';
            case 'CARD': return 'badge-card';
            case 'INSURANCE': return 'badge-insurance';
            case 'BANK_TRANSFER': return 'badge-transfer';
            case 'CHECK': return 'badge-check';
            default: return 'badge-default';
        }
    }

    getMethodLabel(method: string): string {
        const labels: Record<string, string> = {
            'CASH': 'Cash', 'CARD': 'Card', 'INSURANCE': 'Insurance',
            'BANK_TRANSFER': 'Bank Transfer', 'CHECK': 'Check'
        };
        return labels[method] || method;
    }
}
