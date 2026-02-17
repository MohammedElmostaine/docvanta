import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicService } from '../../../services/clinic.service';
import { Clinic } from '../../../models/auth.models';

@Component({
    selector: 'app-clinics',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './clinics.component.html',
    styleUrls: ['./clinics.component.css']
})
export class ClinicsComponent implements OnInit {
    clinics: Clinic[] = [];
    loading = true;
    errorMessage = '';
    showModal = false;
    editingId: number | null = null;
    formData = { name: '', address: '' };

    constructor(private clinicService: ClinicService, private cdr: ChangeDetectorRef) {}

    ngOnInit(): void { this.loadClinics(); }

    loadClinics(): void {
        this.loading = true;
        this.clinicService.getAll().subscribe({
            next: (data) => { this.clinics = data || []; this.loading = false; this.cdr.detectChanges(); },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to load clinics'; this.loading = false; this.cdr.detectChanges(); }
        });
    }

    openCreateModal(): void {
        this.editingId = null;
        this.formData = { name: '', address: '' };
        this.showModal = true;
    }

    openEditModal(clinic: Clinic): void {
        this.editingId = clinic.clinicId;
        this.formData = { name: clinic.name, address: clinic.address };
        this.showModal = true;
    }

    closeModal(): void { this.showModal = false; }

    saveClinic(): void {
        if (this.editingId) {
            this.clinicService.update(this.editingId, this.formData).subscribe({
                next: () => { this.closeModal(); this.loadClinics(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to update'; this.loading = false; }
            });
        } else {
            this.clinicService.create(this.formData).subscribe({
                next: () => { this.closeModal(); this.loadClinics(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to create'; this.loading = false; }
            });
        }
    }

    deleteClinic(id: number): void {
        if (confirm('Delete this clinic?')) {
            this.clinicService.delete(id).subscribe({
                next: () => this.loadClinics(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.loading = false; }
            });
        }
    }
}
