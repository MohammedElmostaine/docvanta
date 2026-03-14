import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../services/auth.service';
import { MedicalRecordService } from '../../../services/medical-record.service';

@Component({
    selector: 'app-patient-record',
    standalone: true,
    imports: [CommonModule],
    templateUrl: './patient-record.component.html',
    styleUrls: ['./patient-record.component.css']
})
export class PatientRecordComponent implements OnInit {
    user: any;
    record: any = null;
    loading = true;
    errorMessage = '';
    noRecord = false;

    constructor(
        private authService: AuthService,
        private medicalRecordService: MedicalRecordService,
        private cdr: ChangeDetectorRef
    ) {
        this.user = this.authService.getUser();
    }

    ngOnInit(): void {
        this.loadRecord();
    }

    loadRecord(): void {
        this.loading = true;
        this.errorMessage = '';
        this.noRecord = false;

        const userId = this.user?.userId;
        if (!userId) {
            this.loading = false;
            this.errorMessage = 'Unable to determine patient account.';
            return;
        }

        this.medicalRecordService.getByPatient(userId).subscribe({
            next: (data) => {
                this.record = data;
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: (err) => {
                this.loading = false;
                // 404 means no record exists — this is a normal state
                if (err?.status === 404 || err?.status === 400) {
                    this.noRecord = true;
                } else {
                    this.errorMessage = 'Failed to load medical record.';
                }
                this.cdr.detectChanges();
            }
        });
    }

    getAllergiesList(): string[] {
        if (!this.record?.allergies) return [];
        return this.record.allergies.split(',').map((a: string) => a.trim()).filter((a: string) => a);
    }

    getChronicDiseasesList(): string[] {
        if (!this.record?.chronicDiseases) return [];
        return this.record.chronicDiseases.split(',').map((d: string) => d.trim()).filter((d: string) => d);
    }
}
