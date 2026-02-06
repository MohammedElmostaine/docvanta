import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, Validators, ReactiveFormsModule, AbstractControl, ValidationErrors } from '@angular/forms';
import { AuthService } from '../../services/auth.service';
import { RegisterRequest } from '../../models/auth.models';

@Component({
    selector: 'app-register',
    standalone: true,
    imports: [CommonModule, RouterLink, ReactiveFormsModule],
    templateUrl: './register.component.html',
    styleUrls: ['./register.component.css']
})
export class RegisterComponent {
    registerForm: FormGroup;
    isLoading = false;
    showPassword = false;
    showConfirmPassword = false;
    registrationSuccess = false;
    errorMessage = '';

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private router: Router
    ) {
        this.registerForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3), Validators.maxLength(50), Validators.pattern(/^\S+$/)]],
            firstName: ['', [Validators.required]],
            lastName: ['', [Validators.required]],
            email: ['', [Validators.required, Validators.email]],
            phone: [''],
            password: ['', [Validators.required, Validators.minLength(6)]],
            confirmPassword: ['', [Validators.required]]
        }, { validators: this.passwordMatchValidator });
    }

    passwordMatchValidator(control: AbstractControl): ValidationErrors | null {
        const password = control.get('password');
        const confirmPassword = control.get('confirmPassword');

        if (password && confirmPassword && password.value !== confirmPassword.value) {
            confirmPassword.setErrors({ passwordMismatch: true });
            return { passwordMismatch: true };
        }
        return null;
    }

    togglePasswordVisibility(field: 'password' | 'confirm'): void {
        if (field === 'password') {
            this.showPassword = !this.showPassword;
        } else {
            this.showConfirmPassword = !this.showConfirmPassword;
        }
    }

    onSubmit(): void {
        if (this.registerForm.valid) {
            this.isLoading = true;
            this.errorMessage = '';

            const formValue = this.registerForm.value;
            const request: RegisterRequest = {
                username: formValue.username,
                password: formValue.password,
                firstName: formValue.firstName,
                lastName: formValue.lastName,
                email: formValue.email,
                phone: formValue.phone || undefined,
                role: 'PATIENT'
            };

            this.authService.register(request).subscribe({
                next: () => {
                    this.isLoading = false;
                    this.registrationSuccess = true;
                    setTimeout(() => {
                        this.router.navigate(['/patient']);
                    }, 2000);
                },
                error: (err) => {
                    this.isLoading = false;
                    if (err.status === 400 && err.error?.data) {
                        // Validation errors from backend
                        const messages = Object.values(err.error.data) as string[];
                        this.errorMessage = messages.join('. ');
                    } else if (err.error?.message) {
                        this.errorMessage = err.error.message;
                    } else {
                        this.errorMessage = 'Registration failed. Please try again.';
                    }
                }
            });
        } else {
            this.registerForm.markAllAsTouched();
        }
    }

    isFieldInvalid(field: string): boolean {
        const control = this.registerForm.get(field);
        return !!(control && control.invalid && control.touched);
    }

    getFieldError(field: string): string {
        const control = this.registerForm.get(field);
        if (control?.errors) {
            if (control.errors['required']) return `${this.getFieldLabel(field)} is required`;
            if (control.errors['email']) return 'Please enter a valid email address';
            if (control.errors['minlength']) return `Minimum ${control.errors['minlength'].requiredLength} characters required`;
            if (control.errors['maxlength']) return `Maximum ${control.errors['maxlength'].requiredLength} characters allowed`;
            if (control.errors['pattern']) return 'Username must not contain spaces';
            if (control.errors['passwordMismatch']) return 'Passwords do not match';
        }
        return '';
    }

    private getFieldLabel(field: string): string {
        const labels: Record<string, string> = {
            username: 'Username',
            firstName: 'First name',
            lastName: 'Last name',
            email: 'Email',
            role: 'Role',
            password: 'Password',
            confirmPassword: 'Confirm password'
        };
        return labels[field] || field;
    }
}
