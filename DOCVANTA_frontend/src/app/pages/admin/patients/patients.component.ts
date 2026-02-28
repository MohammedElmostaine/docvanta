import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { PatientService } from '../../../services/patient.service';
import { Patient } from '../../../models/auth.models';

@Component({
    selector: 'app-patients-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './patients.component.html',
    styleUrls: ['./patients.component.css']
})
export class PatientsComponent implements OnInit {
    patients: Patient[] = [];
    filteredPatients: Patient[] = [];
    loading = true;
    searchQuery = '';
    errorMessage = '';
    successMessage = '';
    editingPatient: Patient | null = null;
    editForm: any = {};

    constructor(private patientService: PatientService, private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.loadPatients();
    }

    loadPatients(): void {
        this.loading = true;
        this.patientService.getAll().subscribe({
            next: (data) => {
                this.patients = data || [];
                this.filteredPatients = [...this.patients];
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load patients';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    onSearch(): void {
        const q = this.searchQuery.toLowerCase().trim();
        if (!q) {
            this.filteredPatients = [...this.patients];
            return;
        }
        this.filteredPatients = this.patients.filter(p =>
            (p.firstName + ' ' + p.lastName).toLowerCase().includes(q) ||
            p.email?.toLowerCase().includes(q) ||
            p.phone?.includes(q)
        );
    }

    deletePatient(id: number): void {
        if (confirm('Are you sure you want to delete this patient?')) {
            this.patientService.delete(id).subscribe({
                next: () => this.loadPatients(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete patient'; this.loading = false; }
            });
        }
    }

    openEdit(patient: Patient): void {
        this.editingPatient = patient;
        this.editForm = {
            firstName: patient.firstName,
            lastName: patient.lastName,
            email: patient.email,
            phone: patient.phone,
            dob: patient.dob,
            address: patient.address,
            enabled: patient.enabled
        };
    }

    closeEdit(): void {
        this.editingPatient = null;
        this.editForm = {};
    }

    saveEdit(): void {
        if (!this.editingPatient) return;
        this.patientService.update(this.editingPatient.userId, this.editForm).subscribe({
            next: () => {
                this.successMessage = 'Patient updated successfully';
                this.closeEdit();
                this.loadPatients();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to update patient';
                this.cdr.detectChanges();
            }
        });
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString();
    }
}
