import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';

@Component({
    selector: 'app-features',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './features.component.html',
    styleUrls: ['./features.component.css']
})
export class FeaturesComponent {
    featureCategories = [
        {
            category: 'Appointment Management',
            icon: 'calendar',
            color: '#0d9488',
            features: [
                { name: 'Smart Scheduling', desc: 'Book appointments based on practitioner availability and specialty' },
                { name: 'Status Tracking', desc: 'Full lifecycle: Pending → Confirmed → Completed → Invoiced → Paid' },
                { name: 'Conflict Prevention', desc: 'Automatic detection of scheduling conflicts and double bookings' },
                { name: 'Multi-Specialty Support', desc: 'Book by specialty with automatic practitioner matching' }
            ]
        },
        {
            category: 'Patient Records',
            icon: 'file-medical',
            color: '#3b82f6',
            features: [
                { name: 'Complete Medical History', desc: 'Blood type, allergies, chronic diseases, and clinical notes' },
                { name: 'Medical Documents', desc: 'Prescriptions, lab reports, and certificates with practitioner signatures' },
                { name: 'Patient Portal', desc: 'Patients can view their own records and authorized documents' },
                { name: 'Document Authorization', desc: 'Practitioners control which documents patients can access' }
            ]
        },
        {
            category: 'Billing & Payments',
            icon: 'credit-card',
            color: '#8b5cf6',
            features: [
                { name: 'Medical Act Catalog', desc: 'Configurable catalog with categories, codes, and base pricing' },
                { name: 'Automatic Invoicing', desc: 'Generate invoices directly from completed appointment acts' },
                { name: 'Flexible Payments', desc: 'Cash, Card, Insurance, Bank Transfer, Check — partial or full' },
                { name: 'Cash Register', desc: 'Daily session management with opening/closing balances' }
            ]
        },
        {
            category: 'Clinic Administration',
            icon: 'settings',
            color: '#f59e0b',
            features: [
                { name: 'User Management', desc: 'Create, edit, enable/disable any user with role assignment' },
                { name: 'Role & Permissions', desc: 'Granular access rights per role with security enforcement' },
                { name: 'Department Organization', desc: 'Organize practitioners into clinical departments' },
                { name: 'Specialty Management', desc: 'Manage specialties and assign them to practitioners' }
            ]
        }
    ];
}
