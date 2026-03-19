import { Component, OnInit, ChangeDetectorRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule, FormBuilder, FormGroup, Validators, ReactiveFormsModule } from '@angular/forms';
import { AuthService } from '../../../services/auth.service';
import { AdminService, AdminUser } from '../../../services/admin.service';
import { RegisterRequest } from '../../../models/auth.models';

@Component({
    selector: 'app-admin-create-user',
    standalone: true,
    imports: [CommonModule, ReactiveFormsModule, FormsModule],
    templateUrl: './create-user.component.html',
    styleUrls: ['./create-user.component.css']
})
export class AdminCreateUserComponent implements OnInit {
    // Create form
    userForm: FormGroup;
    isLoading = false;
    successMessage = '';
    errorMessage = '';
    showCreateForm = false;
    selectedRole = '';

    // User list
    allUsers: AdminUser[] = [];
    filteredUsers: AdminUser[] = [];
    loadingUsers = true;
    searchQuery = '';
    roleFilter = 'ALL';

    // Dynamic roles & specialties
    availableRoles: {roleId: number, name: string}[] = [];
    availableSpecialties: {specialtyId: number, name: string}[] = [];
    selectedSpecialtyIds: number[] = [];

    roles: {value: string, label: string}[] = [];

    private roleLabels: Record<string, string> = {
        'PATIENT': 'Patient',
        'PRACTITIONER': 'Practitioner (Doctor)',
        'RECEPTIONIST': 'Receptionist',
        'SYSTEM_ADMINISTRATOR': 'Administrator'
    };

    constructor(
        private fb: FormBuilder,
        private authService: AuthService,
        private adminService: AdminService,
        private cdr: ChangeDetectorRef
    ) {
        this.userForm = this.fb.group({
            username: ['', [Validators.required, Validators.minLength(3), Validators.pattern(/^\S+$/)]],
            firstName: ['', [Validators.required]],
            lastName: ['', [Validators.required]],
            email: ['', [Validators.required, Validators.email]],
            phone: ['', [Validators.required]],
            role: ['', [Validators.required]],
            password: ['', [Validators.required, Validators.minLength(6)]],
            // Role-specific fields (dynamically required)
            dob: [''],
            address: ['']
        });

        // Listen for role changes to toggle conditional validators
        this.userForm.get('role')!.valueChanges.subscribe((role: string) => {
            this.selectedRole = role;
            this.updateRoleValidators(role);
        });
    }

    ngOnInit(): void {
        this.loadUsers();
        this.adminService.getRoles().subscribe({
            next: (r) => {
                this.availableRoles = r;
                this.roles = this.availableRoles.map(role => ({
                    value: role.name,
                    label: this.roleLabels[role.name] || role.name
                }));
                this.cdr.detectChanges();
            }
        });
        this.adminService.getSpecialties().subscribe({
            next: (s) => {
                this.availableSpecialties = s;
                this.cdr.detectChanges();
            }
        });
    }

    private updateRoleValidators(role: string): void {
        const dobControl = this.userForm.get('dob')!;
        const addressControl = this.userForm.get('address')!;

        // Clear all role-specific validators first
        dobControl.clearValidators();
        addressControl.clearValidators();

        if (role === 'PATIENT') {
            dobControl.setValidators([Validators.required]);
            addressControl.setValidators([Validators.required]);
        }

        // Reset specialty selections when switching roles
        this.selectedSpecialtyIds = [];

        dobControl.updateValueAndValidity();
        addressControl.updateValueAndValidity();
    }

    toggleSpecialty(id: number): void {
        const idx = this.selectedSpecialtyIds.indexOf(id);
        if (idx >= 0) {
            this.selectedSpecialtyIds.splice(idx, 1);
        } else {
            this.selectedSpecialtyIds.push(id);
        }
    }

    loadUsers(): void {
        this.loadingUsers = true;
        this.adminService.getAllUsers().subscribe({
            next: (users) => {
                this.allUsers = users || [];
                this.applyFilters();
                this.loadingUsers = false;
                this.cdr.detectChanges();
            },
            error: () => {
                this.loadingUsers = false;
                this.cdr.detectChanges();
            }
        });
    }

    applyFilters(): void {
        let result = [...this.allUsers];
        const q = this.searchQuery.toLowerCase().trim();

        if (q) {
            result = result.filter(u =>
                u.username?.toLowerCase().includes(q) ||
                u.firstName?.toLowerCase().includes(q) ||
                u.lastName?.toLowerCase().includes(q) ||
                (u.firstName + ' ' + u.lastName).toLowerCase().includes(q)
            );
        }

        if (this.roleFilter !== 'ALL') {
            result = result.filter(u => {
                if (this.roleFilter === 'CLINIC_PERSONNEL') {
                    return u.userType === 'CLINIC_PERSONNEL';
                }
                return u.roleName === this.roleFilter || u.personnelType === this.roleFilter;
            });
        }

        this.filteredUsers = result;
    }

    onSubmit(): void {
        if (!this.userForm.valid) {
            this.userForm.markAllAsTouched();
            return;
        }

        this.isLoading = true;
        this.errorMessage = '';
        this.successMessage = '';

        const v = this.userForm.value;
        const request: RegisterRequest = {
            username: v.username,
            password: v.password,
            firstName: v.firstName,
            lastName: v.lastName,
            email: v.email,
            phone: v.phone,
            role: v.role
        };

        // Add role-specific fields
        if (v.role === 'PATIENT') {
            request.dob = v.dob;
            request.address = v.address;
        } else if (v.role === 'PRACTITIONER') {
            if (this.selectedSpecialtyIds.length === 0) {
                this.errorMessage = 'Please select at least one specialty for the practitioner.';
                this.isLoading = false;
                return;
            }
            request.specialtyIds = this.selectedSpecialtyIds;
        }

        this.authService.adminCreateUser(request).subscribe({
            next: (res) => {
                this.isLoading = false;
                this.successMessage = `User "${res.username}" created successfully as ${this.getRoleLabel(res.role)}`;
                this.userForm.reset();
                this.selectedRole = '';
                this.selectedSpecialtyIds = [];
                this.showCreateForm = false;
                this.loadUsers();
                setTimeout(() => this.successMessage = '', 5000);
            },
            error: (err) => {
                this.isLoading = false;
                this.errorMessage = err.error?.message || err.error?.data?.message || 'Failed to create user';
            }
        });
    }

    toggleEnabled(user: AdminUser): void {
        this.adminService.toggleUserEnabled(user.userId).subscribe({
            next: (updated) => {
                const idx = this.allUsers.findIndex(u => u.userId === user.userId);
                if (idx >= 0) {
                    this.allUsers[idx].enabled = updated.enabled;
                    this.applyFilters();
                }
            },
            error: () => {
                this.errorMessage = 'Failed to update user status';
            }
        });
    }

    deleteUser(user: AdminUser): void {
        if (!confirm(`Are you sure you want to delete user "${user.username}"? This action cannot be undone.`)) return;
        this.adminService.deleteUser(user.userId).subscribe({
            next: () => {
                this.allUsers = this.allUsers.filter(u => u.userId !== user.userId);
                this.applyFilters();
                this.successMessage = `User "${user.username}" deleted successfully`;
                setTimeout(() => this.successMessage = '', 4000);
            },
            error: (err) => {
                this.errorMessage = err.error?.message || 'Failed to delete user';
            }
        });
    }

    isFieldInvalid(field: string): boolean {
        const control = this.userForm.get(field);
        return !!(control && control.invalid && control.touched);
    }

    getRoleLabel(value: string): string {
        return this.roles.find(r => r.value === value)?.label || value;
    }

    getEffectiveRole(user: AdminUser): string {
        return user.personnelType || user.roleName || 'UNKNOWN';
    }
}
