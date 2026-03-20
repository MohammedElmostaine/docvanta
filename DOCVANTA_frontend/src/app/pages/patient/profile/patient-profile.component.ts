import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { PatientService } from '../../../services/patient.service';

@Component({
    selector: 'app-patient-profile',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './patient-profile.component.html',
    styleUrls: ['./patient-profile.component.css']
})
export class PatientProfileComponent implements OnInit {
    user: any;
    patient: any = null;
    loading = true;
    editMode = false;
    saving = false;
    successMessage = '';
    errorMessage = '';

    editData = {
        firstName: '',
        lastName: '',
        email: '',
        phone: '',
        address: '',
        dob: ''
    };

    constructor(
        private authService: AuthService,
        private patientService: PatientService,
        private cdr: ChangeDetectorRef
    ) {
        this.user = this.authService.getUser();
    }

    ngOnInit(): void {
        this.loadProfile();
    }

    private loadProfile(): void {
        this.loading = true;
        const userId = this.user?.userId;
        if (!userId) {
            this.loading = false;
            return;
        }

        this.patientService.getById(userId).subscribe({
            next: (data) => {
                this.patient = data;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loading = false;
                this.errorMessage = 'Failed to load profile information.';
                this.cdr.detectChanges();
            }
        });
    }

    enterEditMode(): void {
        if (!this.patient) return;
        this.editData = {
            firstName: this.patient.firstName || '',
            lastName: this.patient.lastName || '',
            email: this.patient.email || '',
            phone: this.patient.phone || '',
            address: this.patient.address || '',
            dob: this.patient.dob || ''
        };
        this.editMode = true;
        this.errorMessage = '';
        this.successMessage = '';
    }

    cancelEdit(): void {
        this.editMode = false;
        this.errorMessage = '';
    }

    saveProfile(): void {
        this.saving = true;
        this.errorMessage = '';

        this.patientService.update(this.user.userId, this.editData).subscribe({
            next: (data) => {
                this.patient = data;
                this.saving = false;
                this.editMode = false;
                this.successMessage = 'Profile updated successfully!';
                this.cdr.detectChanges();
                setTimeout(() => { this.successMessage = ''; this.cdr.detectChanges(); }, 4000);
            },
            error: (err) => {
                this.saving = false;
                this.errorMessage = err?.error?.message || 'Failed to update profile.';
                this.cdr.detectChanges();
            }
        });
    }

    getInitials(): string {
        if (this.patient?.firstName && this.patient?.lastName) {
            return (this.patient.firstName.charAt(0) + this.patient.lastName.charAt(0)).toUpperCase();
        }
        return this.user?.username?.charAt(0)?.toUpperCase() || 'P';
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return 'Not set';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'long', day: 'numeric', year: 'numeric' });
    }
}
