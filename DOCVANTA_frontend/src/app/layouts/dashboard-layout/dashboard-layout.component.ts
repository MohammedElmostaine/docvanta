import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
    selector: 'app-dashboard-layout',
    standalone: true,
    imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
    templateUrl: './dashboard-layout.component.html',
    styleUrls: ['./dashboard-layout.component.css']
})
export class DashboardLayoutComponent {
    sidebarCollapsed = false;
    userMenuOpen = false;
    user: any;

    navItems: { label: string; icon: string; route: string }[] = [];

    constructor(private authService: AuthService, private router: Router) {
        this.user = this.authService.getUser();
        this.buildNav();
    }

    private buildNav(): void {
        const type = this.user?.role;

        switch (type) {
            case 'SYSTEM_ADMINISTRATOR':
                this.navItems = [
                    { label: 'Dashboard', icon: 'dashboard', route: '/admin' },
                    { label: 'Practitioners', icon: 'doctors', route: '/admin/doctors' },
                    { label: 'Patients', icon: 'patients', route: '/admin/patients' },
                    { label: 'Personnel', icon: 'staff', route: '/admin/staff' },
                    { label: 'Appointments', icon: 'appointments', route: '/admin/appointments' },
                    { label: 'Documents', icon: 'documents', route: '/admin/documents' },
                    { label: 'Clinics', icon: 'clinics', route: '/admin/clinics' },
                    { label: 'Departments', icon: 'clinics', route: '/admin/departments' },
                    { label: 'Medical Acts', icon: 'record', route: '/admin/medical-acts' },
                    { label: 'Create User', icon: 'staff', route: '/admin/create-user' },
                ];
                break;
            case 'RECEPTIONIST':
                this.navItems = [
                    { label: 'Dashboard', icon: 'dashboard', route: '/receptionist' },
                    { label: 'Appointments', icon: 'appointments', route: '/receptionist/appointments' },
                    { label: 'Invoices', icon: 'documents', route: '/receptionist/invoices' },
                    { label: 'Payments', icon: 'clinics', route: '/receptionist/payments' },
                    { label: 'Medical Acts', icon: 'record', route: '/receptionist/medical-acts' },
                    { label: 'Patients', icon: 'patients', route: '/receptionist/patients' },
                ];
                break;
            case 'PRACTITIONER':
                this.navItems = [
                    { label: 'Dashboard', icon: 'dashboard', route: '/practitioner' },
                    { label: 'My Patients', icon: 'patients', route: '/practitioner/patients' },
                    { label: 'Appointments', icon: 'appointments', route: '/practitioner/appointments' },
                    { label: 'Documents', icon: 'documents', route: '/practitioner/documents' },
                ];
                break;
            default:
                // Patient portal
                this.navItems = [
                    { label: 'Dashboard', icon: 'dashboard', route: '/patient' },
                    { label: 'My Appointments', icon: 'appointments', route: '/patient/appointments' },
                    { label: 'My Documents', icon: 'documents', route: '/patient/documents' },
                    { label: 'Medical Record', icon: 'record', route: '/patient/record' },
                    { label: 'My Profile', icon: 'patients', route: '/patient/profile' },
                ];
                break;
        }
    }

    toggleSidebar(): void {
        this.sidebarCollapsed = !this.sidebarCollapsed;
    }

    toggleUserMenu(): void {
        this.userMenuOpen = !this.userMenuOpen;
    }

    logout(): void {
        this.authService.logout();
        this.router.navigate(['/login']);
    }

    getPersonnelLabel(): string {
        const labels: Record<string, string> = {
            'SYSTEM_ADMINISTRATOR': 'Administrator',
            'PRACTITIONER': 'Practitioner',
            'PATIENT': 'Patient',
            'RECEPTIONIST': 'Receptionist',
        };
        return labels[this.user?.role] || 'User';
    }

    getIconSvg(icon: string): string {
        const icons: Record<string, string> = {
            dashboard: '<path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/><polyline points="9 22 9 12 15 12 15 22"/>',
            doctors: '<path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/><circle cx="12" cy="7" r="4"/><line x1="12" y1="11" x2="12" y2="17"/><line x1="9" y1="14" x2="15" y2="14"/>',
            patients: '<path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><path d="M23 21v-2a4 4 0 0 0-3-3.87"/><path d="M16 3.13a4 4 0 0 1 0 7.75"/>',
            staff: '<path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"/><circle cx="9" cy="7" r="4"/><rect x="16" y="11" width="6" height="6" rx="1"/>',
            appointments: '<rect x="3" y="4" width="18" height="18" rx="2" ry="2"/><line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/><line x1="3" y1="10" x2="21" y2="10"/>',
            documents: '<path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"/><polyline points="14 2 14 8 20 8"/><line x1="16" y1="13" x2="8" y2="13"/><line x1="16" y1="17" x2="8" y2="17"/><polyline points="10 9 9 9 8 9"/>',
            clinics: '<path d="M3 21h18"/><path d="M5 21V7l8-4v18"/><path d="M19 21V11l-6-4"/><path d="M9 9h1"/><path d="M9 13h1"/><path d="M9 17h1"/>',
            record: '<path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/><polyline points="16 17 21 12 16 7"/><line x1="21" y1="12" x2="9" y2="12"/>',
        };
        return icons[icon] || icons['dashboard'];
    }
}
