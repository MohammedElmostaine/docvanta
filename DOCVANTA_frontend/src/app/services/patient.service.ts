import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse, Patient } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class PatientService {
    private readonly url = `${API_BASE}/patients`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<Patient[]> {
        return this.http.get<ApiResponse<Patient[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<Patient> {
        return this.http.get<ApiResponse<Patient>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByUsername(username: string): Observable<Patient> {
        return this.http.get<ApiResponse<Patient>>(`${this.url}/username/${username}`).pipe(map(r => r.data));
    }

    search(q: string): Observable<Patient[]> {
        return this.http.get<ApiResponse<Patient[]>>(`${this.url}/search?q=${q}`).pipe(map(r => r.data));
    }

    getByClinic(clinicId: number): Observable<Patient[]> {
        return this.http.get<ApiResponse<Patient[]>>(`${this.url}/clinic/${clinicId}`).pipe(map(r => r.data));
    }

    update(id: number, patient: Partial<Patient>): Observable<Patient> {
        return this.http.put<ApiResponse<Patient>>(`${this.url}/${id}`, patient).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }
}
