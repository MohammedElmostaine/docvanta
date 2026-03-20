import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './guards/auth.guard';

export const routes: Routes = [
    {
        path: '',
        loadComponent: () => import('./pages/landing/landing.component').then(m => m.LandingComponent)
    },
    {
        path: 'login',
        canActivate: [guestGuard],
        loadComponent: () => import('./pages/login/login.component').then(m => m.LoginComponent)
    },
    {
        path: 'register',
        canActivate: [guestGuard],
        loadComponent: () => import('./pages/register/register.component').then(m => m.RegisterComponent)
    },
    {
        path: 'features',
        loadComponent: () => import('./pages/features/features.component').then(m => m.FeaturesComponent)
    },
    {
        path: 'about',
        loadComponent: () => import('./pages/about/about.component').then(m => m.AboutComponent)
    },
    // System Administrator routes
    {
        path: 'admin',
        canActivate: [authGuard, roleGuard(['SYSTEM_ADMINISTRATOR'])],
        loadComponent: () => import('./layouts/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
        children: [
            {
                path: '',
                loadComponent: () => import('./pages/admin/overview/overview.component').then(m => m.AdminOverviewComponent)
            },
            {
                path: 'doctors',
                loadComponent: () => import('./pages/admin/doctors/doctors.component').then(m => m.DoctorsComponent)
            },
            {
                path: 'patients',
                loadComponent: () => import('./pages/admin/patients/patients.component').then(m => m.PatientsComponent)
            },
            {
                path: 'staff',
                loadComponent: () => import('./pages/admin/staff/staff.component').then(m => m.StaffComponent)
            },
            {
                path: 'appointments',
                loadComponent: () => import('./pages/admin/appointments/appointments.component').then(m => m.AppointmentsComponent)
            },
            {
                path: 'documents',
                loadComponent: () => import('./pages/admin/documents/documents.component').then(m => m.DocumentsComponent)
            },
            {
                path: 'clinics',
                loadComponent: () => import('./pages/admin/clinics/clinics.component').then(m => m.ClinicsComponent)
            },
            {
                path: 'departments',
                loadComponent: () => import('./pages/admin/departments/departments.component').then(m => m.DepartmentsComponent)
            },
            {
                path: 'medical-acts',
                loadComponent: () => import('./pages/receptionist/medical-acts/medical-acts.component').then(m => m.MedicalActsComponent)
            },
            {
                path: 'create-user',
                loadComponent: () => import('./pages/admin/create-user/create-user.component').then(m => m.AdminCreateUserComponent)
            }
        ]
    },
    // Receptionist routes
    {
        path: 'receptionist',
        canActivate: [authGuard, roleGuard(['RECEPTIONIST'])],
        loadComponent: () => import('./layouts/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
        children: [
            {
                path: '',
                loadComponent: () => import('./pages/receptionist/dashboard/receptionist-dashboard.component').then(m => m.ReceptionistDashboardComponent)
            },
            {
                path: 'appointments',
                loadComponent: () => import('./pages/receptionist/appointments/receptionist-appointments.component').then(m => m.ReceptionistAppointmentsComponent)
            },
            {
                path: 'invoices',
                loadComponent: () => import('./pages/receptionist/invoices/invoices.component').then(m => m.InvoicesComponent)
            },
            {
                path: 'payments',
                loadComponent: () => import('./pages/receptionist/payments/payments.component').then(m => m.PaymentsComponent)
            },
            {
                path: 'medical-acts',
                loadComponent: () => import('./pages/receptionist/medical-acts/medical-acts.component').then(m => m.MedicalActsComponent)
            },
            {
                path: 'patients',
                loadComponent: () => import('./pages/receptionist/patients/receptionist-patients.component').then(m => m.ReceptionistPatientsComponent)
            }
        ]
    },
    // Practitioner routes
    {
        path: 'practitioner',
        canActivate: [authGuard, roleGuard(['PRACTITIONER'])],
        loadComponent: () => import('./layouts/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
        children: [
            {
                path: '',
                loadComponent: () => import('./pages/doctor/dashboard/doctor-dashboard.component').then(m => m.DoctorDashboardComponent)
            },
            {
                path: 'patients',
                loadComponent: () => import('./pages/doctor/patients/practitioner-patients.component').then(m => m.PractitionerPatientsComponent)
            },
            {
                path: 'appointments',
                loadComponent: () => import('./pages/doctor/appointments/practitioner-appointments.component').then(m => m.PractitionerAppointmentsComponent)
            },
            {
                path: 'documents',
                loadComponent: () => import('./pages/doctor/documents/practitioner-documents.component').then(m => m.PractitionerDocumentsComponent)
            }
        ]
    },
    // Patient routes
    {
        path: 'patient',
        canActivate: [authGuard, roleGuard(['PATIENT'])],
        loadComponent: () => import('./layouts/dashboard-layout/dashboard-layout.component').then(m => m.DashboardLayoutComponent),
        children: [
            {
                path: '',
                loadComponent: () => import('./pages/patient/dashboard/patient-dashboard.component').then(m => m.PatientDashboardComponent)
            },
            {
                path: 'appointments',
                loadComponent: () => import('./pages/patient/appointments/patient-appointments.component').then(m => m.PatientAppointmentsComponent)
            },
            {
                path: 'documents',
                loadComponent: () => import('./pages/patient/documents/patient-documents.component').then(m => m.PatientDocumentsComponent)
            },
            {
                path: 'record',
                loadComponent: () => import('./pages/patient/record/patient-record.component').then(m => m.PatientRecordComponent)
            },
            {
                path: 'profile',
                loadComponent: () => import('./pages/patient/profile/patient-profile.component').then(m => m.PatientProfileComponent)
            }
        ]
    },
    {
        path: '**',
        redirectTo: ''
    }
];

