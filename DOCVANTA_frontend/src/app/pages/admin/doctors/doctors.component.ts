import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PractitionerService } from '../../../services/practitioner.service';
import { Practitioner } from '../../../models/auth.models';

@Component({
    selector: 'app-practitioners-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './doctors.component.html',
    styleUrls: ['./doctors.component.css']
})
export class DoctorsComponent implements OnInit {
    doctors: Practitioner[] = [];
    filteredDoctors: Practitioner[] = [];
    loading = true;
    searchQuery = '';
    errorMessage = '';
    successMessage = '';
    editingDoctor: Practitioner | null = null;
    editForm: any = {};

    constructor(private practitionerService: PractitionerService, private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.loadDoctors();
    }

    loadDoctors(): void {
        this.loading = true;
        this.practitionerService.getAll().subscribe({
            next: (data) => {
                this.doctors = data || [];
                this.filteredDoctors = [...this.doctors];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load practitioners';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onSearch(): void {
        const q = this.searchQuery.toLowerCase().trim();
        if (!q) {
            this.filteredDoctors = [...this.doctors];
            return;
        }
        this.filteredDoctors = this.doctors.filter(d =>
            (d.firstName + ' ' + d.lastName).toLowerCase().includes(q) ||
            d.email?.toLowerCase().includes(q) ||
            d.username?.toLowerCase().includes(q)
        );
    }

    deleteDoctor(id: number): void {
        if (confirm('Are you sure you want to delete this practitioner?')) {
            this.practitionerService.delete(id).subscribe({
                next: () => this.loadDoctors(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete practitioner'; this.loading = false; }
            });
        }
    }

    getSpecialties(doctor: Practitioner): string {
        return doctor.specialties?.join(', ') || 'None';
    }

    openEdit(doctor: Practitioner): void {
        this.editingDoctor = doctor;
        this.editForm = {
            firstName: doctor.firstName,
            lastName: doctor.lastName,
            email: doctor.email,
            phone: doctor.phone || '',
            enabled: doctor.enabled
        };
    }

    closeEdit(): void {
        this.editingDoctor = null;
        this.editForm = {};
    }

    saveEdit(): void {
        if (!this.editingDoctor) return;
        this.practitionerService.update(this.editingDoctor.userId, this.editForm).subscribe({
            next: () => {
                this.successMessage = 'Practitioner updated successfully';
                this.closeEdit();
                this.loadDoctors();
                this.cdr.detectChanges();
                setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 3000);
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to update practitioner';
                this.cdr.detectChanges();
            }
        });
    }
}
