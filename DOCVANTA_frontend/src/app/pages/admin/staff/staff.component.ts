import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ClinicPersonnelService } from '../../../services/clinic-personnel.service';
import { ClinicPersonnel } from '../../../models/auth.models';

@Component({
    selector: 'app-personnel-list',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './staff.component.html',
    styleUrls: ['./staff.component.css']
})
export class StaffComponent implements OnInit {
    staffList: ClinicPersonnel[] = [];
    filteredStaff: ClinicPersonnel[] = [];
    loading = true;
    searchQuery = '';
    positionFilter = 'ALL';
    errorMessage = '';
    successMessage = '';

    // Edit modal
    editingStaff: ClinicPersonnel | null = null;
    editForm: any = {};

    positions = ['RECEPTIONIST', 'SYSTEM_ADMINISTRATOR'];

    constructor(private clinicPersonnelService: ClinicPersonnelService, private cdr: ChangeDetectorRef) {}

    ngOnInit(): void {
        this.loadStaff();
    }

    loadStaff(): void {
        this.loading = true;
        this.clinicPersonnelService.getAll().subscribe({
            next: (data) => {
                this.staffList = data || [];
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to load staff';
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.staffList];
        const q = this.searchQuery.toLowerCase().trim();

        if (q) {
            result = result.filter(s =>
                (s.firstName + ' ' + s.lastName).toLowerCase().includes(q) ||
                s.personnelType?.toLowerCase().includes(q)
            );
        }

        if (this.positionFilter !== 'ALL') {
            result = result.filter(s => s.personnelType === this.positionFilter);
        }

        this.filteredStaff = result;
    }

    onSearch(): void {
        this.applyFilters();
    }

    onPositionFilterChange(): void {
        this.applyFilters();
    }

    openEdit(member: ClinicPersonnel): void {
        this.editingStaff = member;
        this.editForm = {
            firstName: member.firstName,
            lastName: member.lastName,
            personnelType: member.personnelType || '',
            enabled: member.enabled
        };
    }

    closeEdit(): void {
        this.editingStaff = null;
        this.editForm = {};
    }

    saveEdit(): void {
        if (!this.editingStaff) return;
        this.clinicPersonnelService.update(this.editingStaff.userId, this.editForm).subscribe({
            next: () => {
                this.successMessage = 'Staff member updated successfully';
                setTimeout(() => this.successMessage = '', 3000);
                this.closeEdit();
                this.loadStaff();
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to update staff member';
                this.closeEdit();
                this.cdr.detectChanges();
            }
        });
    }

    deleteStaff(id: number): void {
        if (confirm('Are you sure you want to delete this staff member?')) {
            this.clinicPersonnelService.delete(id).subscribe({
                next: () => this.loadStaff(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.loading = false; }
            });
        }
    }

    getPositionClass(position: string): string {
        switch (position?.toUpperCase()) {
            case 'RECEPTIONIST': return 'pos-receptionist';
            case 'NURSE': return 'pos-nurse';
            case 'ASSISTANT': return 'pos-assistant';
            case 'ADMINISTRATOR': return 'pos-admin';
            default: return 'pos-other';
        }
    }
}
