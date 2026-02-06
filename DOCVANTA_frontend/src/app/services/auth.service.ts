import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map, tap } from 'rxjs';

import { LoginRequest, RegisterRequest, AuthResponse } from '../models/auth.models';
import { API_BASE } from '../config/api.config';

@Injectable({
    providedIn: 'root'
})
export class AuthService {
    private readonly API_URL = `${API_BASE}/auth`;

    constructor(private http: HttpClient) { }

    /**
     * Login with username and password
     * POST /api/auth/login
     */
    login(request: LoginRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.API_URL}/login`, request).pipe(
            tap(response => this.handleAuthResponse(response))
        );
    }

    /**
     * Register a new user
     * POST /api/auth/register
     */
    register(request: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<AuthResponse>(`${this.API_URL}/register`, request).pipe(
            tap(response => this.handleAuthResponse(response))
        );
    }

    /**
     * Admin: create any type of user
     * POST /api/auth/admin/create-user
     */
    adminCreateUser(request: RegisterRequest): Observable<AuthResponse> {
        return this.http.post<any>(`${this.API_URL}/admin/create-user`, request).pipe(
            map(res => res.data || res)
        );
    }

    /** Handle successful auth response — store token + user info */
    private handleAuthResponse(response: AuthResponse): void {
        this.setToken(response.token);
        localStorage.setItem('docvanta_user', JSON.stringify({
            userId: response.userId,
            username: response.username,
            role: response.role
        }));
    }

    /** Store JWT token in local storage */
    setToken(token: string): void {
        localStorage.setItem('docvanta_token', token);
    }

    /** Get stored JWT token */
    getToken(): string | null {
        return localStorage.getItem('docvanta_token');
    }

    /** Get stored user info */
    getUser(): { userId: number; username: string; role: string } | null {
        const user = localStorage.getItem('docvanta_user');
        return user ? JSON.parse(user) : null;
    }

    /** Remove token and user info, log out */
    logout(): void {
        localStorage.removeItem('docvanta_token');
        localStorage.removeItem('docvanta_user');
    }

    /** Check if user is authenticated */
    isAuthenticated(): boolean {
        return !!this.getToken();
    }
}
