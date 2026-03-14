import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { PatientRecord, ApiResponse } from '../models/auth.models';

@Injectable({
    providedIn: 'root'
})
export class MedicalRecordService {
    private readonly API_URL = `${API_BASE}/patient-records`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<PatientRecord[]> {
        return this.http.get<ApiResponse<PatientRecord[]>>(this.API_URL).pipe(map(r => r.data));
    }

    getById(id: number): Observable<PatientRecord> {
        return this.http.get<ApiResponse<PatientRecord>>(`${this.API_URL}/${id}`).pipe(map(r => r.data));
    }

    getByPatient(patientId: number): Observable<PatientRecord> {
        return this.http.get<ApiResponse<PatientRecord>>(`${this.API_URL}/patient/${patientId}`).pipe(map(r => r.data));
    }

    create(record: Partial<PatientRecord>): Observable<PatientRecord> {
        return this.http.post<ApiResponse<PatientRecord>>(this.API_URL, record).pipe(map(r => r.data));
    }

    update(id: number, record: Partial<PatientRecord>): Observable<PatientRecord> {
        return this.http.put<ApiResponse<PatientRecord>>(`${this.API_URL}/${id}`, record).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`).pipe(map(r => r.data));
    }
}
