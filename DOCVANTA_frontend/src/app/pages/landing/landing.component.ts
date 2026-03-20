import { Component, OnInit } from '@angular/core';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-landing',
    standalone: true,
    imports: [CommonModule, RouterLink],
    templateUrl: './landing.component.html',
    styleUrls: ['./landing.component.css']
})
export class LandingComponent implements OnInit {

    constructor(private authService: AuthService, private router: Router) {}

    ngOnInit(): void {
        if (this.authService.isAuthenticated()) {
            const user = this.authService.getUser();
            const role = user?.role;
            if (role) {
                this.router.navigate(['/']);
            }
        }
    }
    features = [
        {
            icon: 'users',
            title: 'Practitioner & Personnel Management',
            description: 'Efficiently manage your entire medical team with comprehensive profiles and role assignments.'
        },
        {
            icon: 'calendar',
            title: 'Smart Appointment Scheduling',
            description: 'Intelligent booking system with automated reminders and conflict prevention.'
        },
        {
            icon: 'file-medical',
            title: 'Secure Patient Records',
            description: 'Store and access patient medical history with enterprise-grade encryption.'
        },
        {
            icon: 'document',
            title: 'Medical Documents Editor',
            description: 'Create, edit, and manage prescriptions and medical documents seamlessly.'
        },
        {
            icon: 'shield',
            title: 'Role-Based Access Control',
            description: 'Granular permissions based on roles and competencies for maximum security.'
        },
        {
            icon: 'portal',
            title: 'Patient Portal',
            description: 'Secure access for patients to view appointments and authorized medical records.'
        }
    ];

    steps = [
        {
            number: 1,
            title: 'Create Your Clinic Workspace',
            description: 'Set up your clinic profile and configure your workspace in minutes.'
        },
        {
            number: 2,
            title: 'Add Practitioners, Personnel & Schedules',
            description: 'Onboard your team members and set up their availability schedules.'
        },
        {
            number: 3,
            title: 'Manage Patients Efficiently',
            description: 'Start managing appointments, records, and medical activities seamlessly.'
        }
    ];

    trustFeatures = [
        {
            icon: 'privacy',
            title: 'Medical Data Privacy',
            description: 'Your patient data is protected with industry-leading encryption standards.'
        },
        {
            icon: 'lock',
            title: 'Complete Confidentiality',
            description: 'Strict access controls ensure only authorized personnel can access sensitive information.'
        },
        {
            icon: 'access',
            title: 'Permission-Based Access',
            description: 'Fine-grained role and competency-based permissions for every user.'
        }
    ];

    personas = [
        {
            role: 'patient',
            title: 'For Patients',
            subtitle: 'Your health journey, simplified',
            icon: 'heart',
            color: '#10b981',
            benefits: [
                'Book appointments online with your preferred practitioner',
                'Access your medical records and documents securely',
                'Track appointment history and upcoming visits',
                'View prescriptions and medical reports anytime'
            ]
        },
        {
            role: 'practitioner',
            title: 'For Practitioners',
            subtitle: 'Focus on care, not paperwork',
            icon: 'stethoscope',
            color: '#3b82f6',
            benefits: [
                'Manage your schedule and availability effortlessly',
                'Access complete patient history during consultations',
                'Create and sign medical documents digitally',
                'Track performed acts and consultation outcomes'
            ]
        },
        {
            role: 'receptionist',
            title: 'For Receptionists',
            subtitle: 'Streamline front-desk operations',
            icon: 'clipboard',
            color: '#8b5cf6',
            benefits: [
                'Manage appointments and patient check-ins',
                'Generate invoices from completed consultations',
                'Process payments with multiple payment methods',
                'Track revenue and view daily summaries'
            ]
        },
        {
            role: 'admin',
            title: 'For Clinic Owners',
            subtitle: 'Complete visibility and control',
            icon: 'building',
            color: '#f59e0b',
            benefits: [
                'Manage all users, roles, and permissions centrally',
                'Oversee departments, specialties, and staff assignments',
                'Monitor clinic activity and financial operations',
                'Configure medical act catalog and pricing'
            ]
        }
    ];

    stats = [
        { value: '4', label: 'User Roles', suffix: '' },
        { value: '100', label: 'Secure Endpoints', suffix: '+' },
        { value: '24/7', label: 'Access', suffix: '' },
        { value: '0', label: 'Data Compromises', suffix: '' }
    ];

    billingSteps = [
        { step: 1, title: 'Consultation', description: 'Practitioner performs medical acts during the appointment', icon: 'stethoscope' },
        { step: 2, title: 'Invoice Generation', description: 'Receptionist generates an invoice from completed acts', icon: 'file' },
        { step: 3, title: 'Payment Collection', description: 'Record full or partial payments with multiple methods', icon: 'wallet' },
        { step: 4, title: 'Financial Tracking', description: 'Track revenue and view daily summaries', icon: 'chart' }
    ];
}
