import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
    const token = localStorage.getItem('docvanta_token');
    const router = inject(Router);

    const request = token
        ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } })
        : req;

    return next(request).pipe(
        tap({
            error: (err) => {
                // Only 401 (unauthorized / expired token) should trigger logout
                // 403 (forbidden) means the user is authenticated but lacks permission — don't logout
                if (err.status === 401 && !req.url.includes('/api/auth/')) {
                    localStorage.removeItem('docvanta_token');
                    localStorage.removeItem('docvanta_user');
                    router.navigate(['/login']);
                }
            }
        })
    );
};
