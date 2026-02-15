import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse, Clinic } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class ClinicService {
    private readonly url = `${API_BASE}/clinics`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<Clinic[]> {
        return this.http.get<ApiResponse<Clinic[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<Clinic> {
        return this.http.get<ApiResponse<Clinic>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    create(clinic: Partial<Clinic>): Observable<Clinic> {
        return this.http.post<ApiResponse<Clinic>>(this.url, clinic).pipe(map(r => r.data));
    }

    update(id: number, clinic: Partial<Clinic>): Observable<Clinic> {
        return this.http.put<ApiResponse<Clinic>>(`${this.url}/${id}`, clinic).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }
}
