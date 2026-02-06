import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.models';

@Component({
    selector: 'app-login',
    standalone: true,
    imports: [CommonModule, RouterLink, ReactiveFormsModule],
    templateUrl: './login.component.html',
    styleUrls: ['./login.component.css']
})
export class LoginComponent {
    loginForm: FormGroup;
    isLoading = false;
    showPassword = false;
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.loginForm = this.fb.group({
            username: ['', [Validators.required]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            rememberMe: [false]
        });
    }

    togglePasswordVisibility(): void {
        this.showPassword = !this.showPassword;
    }

    onSubmit(): void {
        if (this.loginForm.valid) {
            this.isLoading = true;
            this.errorMessage = '';

            const request: LoginRequest = {
                username: this.loginForm.value.username,
                password: this.loginForm.value.password
            };

            this.authService.login(request).subscribe({
                next: (response) => {
                    this.isLoading = false;
                    // Route based on role
                    const routeMap: Record<string, string> = {
                        'SYSTEM_ADMINISTRATOR': '/admin',
                        'PRACTITIONER': '/practitioner',
                        'PATIENT': '/patient',
                        'RECEPTIONIST': '/receptionist',
                    };
                    this.router.navigate([routeMap[response.role] || '/']);
                },
                error: (err) => {
                    this.isLoading = false;
                    if (err.status === 401) {
                        this.errorMessage = 'Invalid username/email or password.';
                    } else if (err.status === 400 && err.error?.data) {
                        // Validation errors from backend
                        const messages = Object.values(err.error.data) as string[];
                        this.errorMessage = messages.join('. ');
                    } else if (err.error?.message) {
                        this.errorMessage = err.error.message;
                    } else {
                        this.errorMessage = 'Login failed. Please check your connection and try again.';
                    }
                }
            });
        } else {
            this.loginForm.markAllAsTouched();
        }
    }

    isFieldInvalid(field: string): boolean {
        const control = this.loginForm.get(field);
        return !!(control && control.invalid && control.touched);
    }
}
