import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { MedicalActService } from '../../../services/medical-act.service';
import { AuthService } from '../../../services/auth.service';
import { MedicalAct, MedicalActRequest, MedicalActCategory } from '../../../models/billing.models';

@Component({
    selector: 'app-medical-acts',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './medical-acts.component.html',
    styleUrls: ['./medical-acts.component.css']
})
export class MedicalActsComponent implements OnInit {
    medicalActs: MedicalAct[] = [];
    filteredActs: MedicalAct[] = [];
    loading = true;
    errorMessage = '';
    successMessage = '';

    searchQuery = '';
    selectedCategory = 'ALL';

    categories: MedicalActCategory[] = [
        'CONSULTATION', 'PROCEDURE', 'LAB_TEST', 'IMAGING', 'TREATMENT', 'SURGERY', 'OTHER'
    ];

    // Modal state
    showModal = false;
    editingAct: MedicalAct | null = null;

    // Form model
    form: MedicalActRequest = {
        code: '',
        name: '',
        description: '',
        category: 'CONSULTATION',
        basePrice: 0,
        active: true
    };

    isAdmin = false;

    constructor(
        private medicalActService: MedicalActService,
        private authService: AuthService,
        private cdr: ChangeDetectorRef
    ) {
        const user = this.authService.getUser();
        this.isAdmin = user?.role === 'SYSTEM_ADMINISTRATOR';
    }

    ngOnInit(): void {
        this.loadActs();
    }

    loadActs(): void {
        this.loading = true;
        this.errorMessage = '';
        this.medicalActService.getAll().subscribe({
            next: (data) => {
                this.medicalActs = data || [];
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load medical acts';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.medicalActs];
        const q = this.searchQuery.toLowerCase().trim();

        if (q) {
            result = result.filter(a =>
                a.code.toLowerCase().includes(q) ||
                a.name.toLowerCase().includes(q) ||
                a.description?.toLowerCase().includes(q)
            );
        }

        if (this.selectedCategory !== 'ALL') {
            result = result.filter(a => a.category === this.selectedCategory);
        }

        this.filteredActs = result;
    }

    onSearch(): void {
        this.applyFilters();
    }

    onCategoryChange(): void {
        this.applyFilters();
    }

    openCreateModal(): void {
        this.editingAct = null;
        this.form = {
            code: '',
            name: '',
            description: '',
            category: 'CONSULTATION',
            basePrice: 0,
            active: true
        };
        this.showModal = true;
    }

    openEditModal(act: MedicalAct): void {
        this.editingAct = act;
        this.form = {
            code: act.code,
            name: act.name,
            description: act.description || '',
            category: act.category,
            basePrice: act.basePrice,
            active: act.active
        };
        this.showModal = true;
    }

    closeModal(): void {
        this.showModal = false;
        this.editingAct = null;
    }

    onCodeInput(): void {
        this.form.code = this.form.code.toUpperCase();
    }

    saveAct(): void {
        this.errorMessage = '';
        this.successMessage = '';

        if (this.editingAct) {
            this.medicalActService.update(this.editingAct.medicalActId, this.form).subscribe({
                next: () => {
                    this.successMessage = 'Medical act updated successfully';
                    this.closeModal();
                    this.loadActs();
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    this.errorMessage = err.error?.message || 'Failed to update medical act';
                    this.cdr.detectChanges();
                }
            });
        } else {
            this.medicalActService.create(this.form).subscribe({
                next: () => {
                    this.successMessage = 'Medical act created successfully';
                    this.closeModal();
                    this.loadActs();
                    this.cdr.detectChanges();
                },
                error: (err) => {
                    this.errorMessage = err.error?.message || 'Failed to create medical act';
                    this.cdr.detectChanges();
                }
            });
        }
    }

    deleteAct(act: MedicalAct): void {
        if (!confirm(`Are you sure you want to deactivate "${act.name}" (${act.code})?`)) {
            return;
        }

        this.errorMessage = '';
        this.successMessage = '';
        this.medicalActService.delete(act.medicalActId).subscribe({
            next: () => {
                this.successMessage = 'Medical act deactivated successfully';
                this.loadActs();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to deactivate medical act';
                this.cdr.detectChanges();
            }
        });
    }

    formatPrice(price: number): string {
        return price.toFixed(2) + ' MAD';
    }

    getCategoryColor(category: string): string {
        const colors: Record<string, string> = {
            CONSULTATION: '#2563eb',
            PROCEDURE: '#7c3aed',
            LAB_TEST: '#059669',
            IMAGING: '#d97706',
            TREATMENT: '#dc2626',
            SURGERY: '#be185d',
            OTHER: '#6b7280'
        };
        return colors[category] || '#6b7280';
    }
}
