import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { DocumentService } from '../../../services/document.service';
import { PatientService } from '../../../services/patient.service';
import { MedicalDocument, MedicalDocumentRequest, Patient } from '../../../models/auth.models';

@Component({
    selector: 'app-practitioner-documents',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './practitioner-documents.component.html',
    styleUrls: ['./practitioner-documents.component.css']
})
export class PractitionerDocumentsComponent implements OnInit {
    user: any;
    documents: MedicalDocument[] = [];
    filteredDocuments: MedicalDocument[] = [];
    patients: Patient[] = [];
    loading = true;
    errorMessage = '';
    successMessage = '';

    typeFilter = 'ALL';
    searchQuery = '';

    showCreateModal = false;
    showPreviewModal = false;
    previewDocument: MedicalDocument | null = null;
    editingId: number | null = null;

    formData: MedicalDocumentRequest = {
        type: 'PRESCRIPTION',
        content: '',
        patientId: 0,
        practitionerId: 0,
        authorizedForPatient: false
    };

    documentTypes = ['PRESCRIPTION', 'MEDICAL_REPORT', 'CERTIFICATE'];

    constructor(
        private authService: AuthService,
        private documentService: DocumentService,
        private patientService: PatientService,
        private cdr: ChangeDetectorRef
    ) {}

    ngOnInit(): void {
        this.user = this.authService.getUser();
        this.loadData();
    }

    loadData(): void {
        this.loading = true;
        let completed = 0;
        const checkDone = () => { completed++; if (completed >= 2) { this.loading = false; this.cdr.detectChanges(); } };

        const practitionerId = this.user?.userId;
        if (!practitionerId) { this.loading = false; return; }

        this.documentService.getByPractitioner(practitionerId).subscribe({
            next: (data) => {
                this.documents = (data || []).sort((a, b) => new Date(b.createdDate).getTime() - new Date(a.createdDate).getTime());
                this.applyFilters();
                checkDone();
            },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to load documents'; checkDone(); }
        });

        this.patientService.getAll().subscribe({
            next: (data) => { this.patients = data || []; checkDone(); },
            error: () => checkDone()
        });
    }

    applyFilters(): void {
        let result = [...this.documents];
        if (this.typeFilter !== 'ALL') {
            result = result.filter(d => d.type === this.typeFilter);
        }
        if (this.searchQuery) {
            const q = this.searchQuery.toLowerCase();
            result = result.filter(d =>
                (d.patientName || '').toLowerCase().includes(q) ||
                (d.type || '').toLowerCase().includes(q) ||
                (d.content || '').toLowerCase().includes(q)
            );
        }
        this.filteredDocuments = result;
    }

    openCreateModal(): void {
        this.editingId = null;
        this.formData = {
            type: 'PRESCRIPTION',
            content: '',
            patientId: this.patients.length > 0 ? this.patients[0].userId : 0,
            practitionerId: this.user?.userId || 0,
            authorizedForPatient: false
        };
        this.showCreateModal = true;
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
        this.showCreateModal = true;
    }

    closeCreateModal(): void { this.showCreateModal = false; }

    saveDocument(): void {
        if (this.editingId) {
            this.documentService.update(this.editingId, this.formData).subscribe({
                next: () => { this.closeCreateModal(); this.showSuccess('Document updated'); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to update'; this.cdr.detectChanges(); }
            });
        } else {
            this.documentService.create(this.formData).subscribe({
                next: () => { this.closeCreateModal(); this.showSuccess('Document created'); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to create'; this.cdr.detectChanges(); }
            });
        }
    }

    toggleAuthorization(doc: MedicalDocument): void {
        this.documentService.authorize(doc.documentId, !doc.authorizedForPatient).subscribe({
            next: () => { this.showSuccess('Authorization updated'); this.loadData(); },
            error: (err) => { this.errorMessage = err.error?.message || 'Failed to update authorization'; this.cdr.detectChanges(); }
        });
    }

    previewDoc(doc: MedicalDocument): void {
        this.previewDocument = doc;
        this.showPreviewModal = true;
    }

    closePreview(): void { this.showPreviewModal = false; this.previewDocument = null; }

    printDoc(doc: MedicalDocument): void {
        const printWindow = window.open('', '_blank');
        if (printWindow) {
            printWindow.document.write(`
                <html><head><title>${doc.type} - ${doc.patientName}</title>
                <style>body{font-family:Arial,sans-serif;padding:2rem;max-width:800px;margin:0 auto}
                h1{font-size:1.5rem;color:#1a1a2e;border-bottom:2px solid #0d9488;padding-bottom:0.5rem}
                .meta{color:#666;font-size:0.875rem;margin-bottom:1.5rem}
                .content{line-height:1.8;white-space:pre-wrap}</style></head>
                <body><h1>${doc.type}</h1>
                <div class="meta">Patient: ${doc.patientName} | Date: ${this.formatDate(doc.createdDate)}</div>
                <div class="content">${doc.content}</div></body></html>`);
            printWindow.document.close();
            printWindow.print();
        }
    }

    deleteDoc(id: number): void {
        if (confirm('Delete this document?')) {
            this.documentService.delete(id).subscribe({
                next: () => { this.showSuccess('Document deleted'); this.loadData(); },
                error: (err) => { this.errorMessage = err.error?.message || 'Failed to delete'; this.cdr.detectChanges(); }
            });
        }
    }

    private showSuccess(msg: string): void {
        this.successMessage = msg;
        setTimeout(() => this.successMessage = '', 3000);
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return '-';
        return new Date(dateStr).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
    }

    getTypeLabel(type: string): string {
        switch (type) {
            case 'PRESCRIPTION': return 'Prescription';
            case 'MEDICAL_REPORT': return 'Medical Report';
            case 'CERTIFICATE': return 'Certificate';
            default: return type;
        }
    }

    getTypeClass(type: string): string {
        switch (type) {
            case 'PRESCRIPTION': return 'type-prescription';
            case 'MEDICAL_REPORT': return 'type-report';
            case 'CERTIFICATE': return 'type-certificate';
            default: return 'type-default';
        }
    }
}
