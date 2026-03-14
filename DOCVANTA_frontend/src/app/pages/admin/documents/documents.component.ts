import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { DocumentService } from '../../../services/document.service';
import { PractitionerService } from '../../../services/practitioner.service';
import { PatientService } from '../../../services/patient.service';
import { MedicalDocument, MedicalDocumentRequest, Practitioner, Patient } from '../../../models/auth.models';

@Component({
    selector: 'app-documents',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './documents.component.html',
    styleUrls: ['./documents.component.css']
})
export class DocumentsComponent implements OnInit {
    documents: MedicalDocument[] = [];
    filteredDocuments: MedicalDocument[] = [];
    practitioners: Practitioner[] = [];
    patients: Patient[] = [];
    loading = true;
    searchQuery = '';
    typeFilter = '';
    errorMessage = '';

    showModal = false;
    showPreview = false;
    editingId: number | null = null;
    previewDoc: MedicalDocument | null = null;

    formData: MedicalDocumentRequest = { type: '', content: '', patientId: 0, practitionerId: 0, authorizedForPatient: false };
    docTypes = ['CERTIFICATE', 'PRESCRIPTION', 'REPORT'];

    constructor(
        private documentService: DocumentService,
        private practitionerService: PractitionerService,
        private patientService: PatientService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void { this.loadData(); }

    loadData(): void {
        this.loading = true;
        let done = 0;
        const check = () => { done++; if (done >= 3) { this.loading = false; this.cdr.detectChanges(); } };

        this.documentService.getAll().subscribe({
            next: d => { this.documents = d || []; this.applyFilters(); check(); },
            error: () => check()
        });
        this.practitionerService.getAll().subscribe({ next: d => { this.practitioners = d || []; check(); }, error: () => check() });
        this.patientService.getAll().subscribe({ next: p => { this.patients = p || []; check(); }, error: () => check() });
    }

    applyFilters(): void {
        let list = [...this.documents];
        const q = this.searchQuery.toLowerCase().trim();
        if (q) {
            list = list.filter(d =>
                d.patientName?.toLowerCase().includes(q) ||
                d.practitionerName?.toLowerCase().includes(q) ||
                d.type?.toLowerCase().includes(q)
            );
        }
        if (this.typeFilter) { list = list.filter(d => d.type === this.typeFilter); }
        this.filteredDocuments = list;
    }

    onSearch(): void { this.applyFilters(); }
    onTypeFilter(): void { this.applyFilters(); }

    openCreateModal(): void {
        this.editingId = null;
        this.formData = { type: '', content: '', patientId: 0, practitionerId: 0, authorizedForPatient: false };
        this.showModal = true;
    }

    openEditModal(doc: MedicalDocument): void {
        this.editingId = doc.documentId;
        this.formData = {
            type: doc.type,
            content: doc.content,
            patientId: doc.patientId,
            practitionerId: doc.practitionerId,
            authorizedForPatient: doc.authorizedForPatient
        };
        this.showModal = true;
    }

    closeModal(): void { this.showModal = false; }

    saveDocument(): void {
        if (this.editingId) {
            this.documentService.update(this.editingId, this.formData).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to update'; this.loading = false; }
            });
        } else {
            this.documentService.create(this.formData).subscribe({
                next: () => { this.closeModal(); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to create'; this.loading = false; }
            });
        }
    }

    toggleAuthorization(doc: MedicalDocument): void {
        this.documentService.authorize(doc.documentId, !doc.authorizedForPatient).subscribe({
            next: () => this.loadData(),
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to update authorization'; this.loading = false; }
        });
    }

    previewDocument(doc: MedicalDocument): void {
        this.previewDoc = doc;
        this.showPreview = true;
    }

    closePreview(): void { this.showPreview = false; this.previewDoc = null; }

    printDocument(): void {
        window.print();
    }

    deleteDocument(id: number): void {
        if (confirm('Delete this document?')) {
            this.documentService.delete(id).subscribe({
                next: () => this.loadData(),
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.loading = false; }
            });
        }
    }

    formatDate(dt: string): string {
        return dt ? new Date(dt).toLocaleDateString() : 'N/A';
    }
}
