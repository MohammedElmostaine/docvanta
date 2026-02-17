import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicalDepartmentService } from '../../../services/clinical-department.service';
import { ClinicService } from '../../../services/clinic.service';
import { ClinicalDepartment, Clinic } from '../../../models/auth.models';

@Component({
    selector: 'app-departments',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './departments.component.html',
    styleUrls: ['./departments.component.css']
})
export class DepartmentsComponent implements OnInit {
    departments: ClinicalDepartment[] = [];
    clinics: Clinic[] = [];
    loading = true;
    errorMessage = '';
    showModal = false;
    editingId: number | null = null;
    formData = { name: '', clinicId: 0 };

    constructor(
        private departmentService: ClinicalDepartmentService,
        private clinicService: ClinicService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const checkDone = () => { completed++; if (completed >= 2) { this.loading = false; this.cdr.detectChanges(); } };

        this.departmentService.getAll().subscribe({
            next: (data) => { this.departments = data || []; checkDone(); },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to load departments'; checkDone(); }
        });

        this.clinicService.getAll().subscribe({
            next: (data) => { this.clinics = data || []; checkDone(); },
            error: () => checkDone()
        });
    }

    openCreateModal(): void {
        this.editingId = null;
        this.formData = { name: '', clinicId: this.clinics.length > 0 ? this.clinics[0].clinicId : 0 };
        this.showModal = true;
    }

    openEditModal(dept: ClinicalDepartment): void {
        this.editingId = dept.departmentId;
        this.formData = { name: dept.name, clinicId: dept.clinicId };
        this.showModal = true;
    }

    closeModal(): void { this.showModal = false; }

    saveDepartment(): void {
        if (this.editingId) {
            this.departmentService.update(this.editingId, this.formData).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to update'; this.loading = false; }
            });
        } else {
            this.departmentService.create(this.formData).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to create'; this.loading = false; }
            });
        }
    }

    deleteDepartment(id: number): void {
        if (confirm('Delete this department?')) {
            this.departmentService.delete(id).subscribe({
                next: () => this.loadData(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.loading = false; }
            });
        }
    }
}
