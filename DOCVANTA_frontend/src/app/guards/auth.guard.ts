import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (authService.isAuthenticated()) {
        return true;
    }

    router.navigate(['/login']);
    return false;
};

export const roleGuard = (allowedRoles: string[]): CanActivateFn => {
    return () => {
        const authService = inject(AuthService);
        const router = inject(Router);

        if (!authService.isAuthenticated()) {
            router.navigate(['/login']);
            return false;
        }

        const user = authService.getUser();
        if (user && allowedRoles.includes(user.role)) {
            return true;
        }

        router.navigate(['/']);
        return false;
    };
};

const roleRouteMap: Record<string, string> = {
    'SYSTEM_ADMINISTRATOR': '/admin',
    'PRACTITIONER': '/practitioner',
    'PATIENT': '/patient',
    'RECEPTIONIST': '/receptionist',
    'CLINICAL_ASSISTANT': '/assistant',
    'NURSE': '/nurse',
    'TECHNICIAN': '/technician',
    'PHARMACIST': '/pharmacist',
};

export const guestGuard: CanActivateFn = () => {
    const authService = inject(AuthService);
    const router = inject(Router);

    if (!authService.isAuthenticated()) {
        return true;
    }

    const user = authService.getUser();
    if (user) {
        const route = roleRouteMap[user.role] || '/';
        router.navigate([route]);
    }
    return false;
};
