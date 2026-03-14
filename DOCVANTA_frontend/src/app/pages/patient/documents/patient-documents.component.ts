import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { DocumentService } from '../../../services/document.service';

@Component({
    selector: 'app-patient-documents',
    standalone: true,
    imports: [CommonModule, FormsModule],
    templateUrl: './patient-documents.component.html',
    styleUrls: ['./patient-documents.component.css']
})
export class PatientDocumentsComponent implements OnInit {
    user: any;
    documents: any[] = [];
    filteredDocuments: any[] = [];
    loading = true;
    typeFilter = 'ALL';
    searchTerm = '';
    selectedDocument: any = null;

    constructor(
        private authService: AuthService,
        private documentService: DocumentService,
        private cdr: ChangeDetectorRef
    ) {
        this.user = this.authService.getUser();
    }

    ngOnInit(): void {
        this.loadDocuments();
    }

    private loadDocuments(): void {
        this.loading = true;
        const userId = this.user?.userId;
        if (!userId) {
            this.loading = false;
            return;
        }

        this.documentService.getAuthorizedForPatient(userId).subscribe({
            next: (data) => {
                this.documents = (data || []).sort(
                    (a: any, b: any) => new Date(b.createdDate || 0).getTime() - new Date(a.createdDate || 0).getTime()
                );
                this.applyFilters();
                this.loading = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.documents = [];
                this.filteredDocuments = [];
                this.loading = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.documents];

        if (this.typeFilter !== 'ALL') {
            result = result.filter(d => d.type === this.typeFilter);
        }

        if (this.searchTerm.trim()) {
            const term = this.searchTerm.toLowerCase();
            result = result.filter(d =>
                (d.practitionerName || '').toLowerCase().includes(term) ||
                (d.type || '').toLowerCase().includes(term) ||
                (d.content || '').toLowerCase().includes(term)
            );
        }

        this.filteredDocuments = result;
    }

    getDocTypeClass(type: string): string {
        switch (type?.toUpperCase()) {
            case 'CERTIFICATE': return 'type-cert';
            case 'PRESCRIPTION': return 'type-presc';
            case 'REPORT': return 'type-report';
            default: return 'type-other';
        }
    }

    formatDate(dateStr: string): string {
        if (!dateStr) return 'N/A';
        return new Date(dateStr).toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' });
    }

    getContentPreview(content: string): string {
        if (!content) return 'No content available';
        return content.length > 120 ? content.substring(0, 120) + '...' : content;
    }

    openPreview(doc: any): void {
        this.selectedDocument = doc;
    }

    closePreview(): void {
        this.selectedDocument = null;
    }

    printDocument(doc: any): void {
        const printWindow = window.open('', '_blank');
        if (printWindow) {
            printWindow.document.write(`
                <html>
                <head>
                    <title>DocVanta - ${doc.type || 'Document'}</title>
                    <style>
                        body { font-family: 'Inter', Arial, sans-serif; padding: 40px; line-height: 1.8; color: #1e293b; }
                        h1 { font-size: 1.5rem; margin-bottom: 20px; color: #0ea5e9; }
                        .meta { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-bottom: 24px; padding: 16px; background: #f8fafc; border-radius: 8px; }
                        .meta-item { font-size: 0.85rem; }
                        .meta-label { color: #64748b; font-weight: 600; text-transform: uppercase; font-size: 0.75rem; }
                        .content { white-space: pre-wrap; border-top: 1px solid #e2e8f0; padding-top: 20px; }
                    </style>
                </head>
                <body>
                    <h1>${doc.type || 'Medical Document'}</h1>
                    <div class="meta">
                        <div class="meta-item"><span class="meta-label">Practitioner:</span><br>${doc.practitionerName || 'N/A'}</div>
                        <div class="meta-item"><span class="meta-label">Date:</span><br>${this.formatDate(doc.createdDate)}</div>
                        <div class="meta-item"><span class="meta-label">Type:</span><br>${doc.type || 'N/A'}</div>
                        <div class="meta-item"><span class="meta-label">Patient:</span><br>${doc.patientName || 'N/A'}</div>
                    </div>
                    <div class="content">${doc.content || 'No content'}</div>
                </body>
                </html>
            `);
            printWindow.document.close();
            printWindow.print();
        }
    }
}
