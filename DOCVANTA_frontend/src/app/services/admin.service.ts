import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';

export interface AdminUser {
    userId: number;
    username: string;
    enabled: boolean;
    roleName: string;
    userType: string;
    firstName: string;
    lastName: string;
    email?: string;
    phone?: string;
    personnelType?: string;
}

@Injectable({
    providedIn: 'root'
})
export class AdminService {
    private readonly API_URL = `${API_BASE}/admin`;

    constructor(private http: HttpClient) {}

    getAllUsers(): Observable<AdminUser[]> {
        return this.http.get<any>(`${this.API_URL}/users`).pipe(
            map(res => res.data || res)
        );
    }

    deleteUser(id: number): Observable<any> {
        return this.http.delete<any>(`${this.API_URL}/users/${id}`);
    }

    toggleUserEnabled(id: number): Observable<AdminUser> {
        return this.http.put<any>(`${this.API_URL}/users/${id}/toggle-enabled`, {}).pipe(
            map(res => res.data || res)
        );
    }

    // Roles
    getRoles(): Observable<{roleId: number, name: string}[]> {
        return this.http.get<any>(`${this.API_URL}/roles`).pipe(map(res => res.data || res));
    }
    createRole(name: string): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/roles`, { name }).pipe(map(res => res.data || res));
    }
    deleteRole(id: number): Observable<any> {
        return this.http.delete<any>(`${this.API_URL}/roles/${id}`);
    }

    // Specialties
    getSpecialties(): Observable<{specialtyId: number, name: string}[]> {
        return this.http.get<any>(`${this.API_URL}/specialties`).pipe(map(res => res.data || res));
    }
    createSpecialty(name: string): Observable<any> {
        return this.http.post<any>(`${this.API_URL}/specialties`, { name }).pipe(map(res => res.data || res));
    }
    deleteSpecialty(id: number): Observable<any> {
        return this.http.delete<any>(`${this.API_URL}/specialties/${id}`);
    }
}
