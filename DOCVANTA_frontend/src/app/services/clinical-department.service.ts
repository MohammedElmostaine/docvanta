import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse, ClinicalDepartment } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class ClinicalDepartmentService {
    private readonly url = `${API_BASE}/clinical-departments`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<ClinicalDepartment[]> {
        return this.http.get<ApiResponse<ClinicalDepartment[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<ClinicalDepartment> {
        return this.http.get<ApiResponse<ClinicalDepartment>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByClinic(clinicId: number): Observable<ClinicalDepartment[]> {
        return this.http.get<ApiResponse<ClinicalDepartment[]>>(`${this.url}/clinic/${clinicId}`).pipe(map(r => r.data));
    }

    create(department: Partial<ClinicalDepartment>): Observable<ClinicalDepartment> {
        return this.http.post<ApiResponse<ClinicalDepartment>>(this.url, department).pipe(map(r => r.data));
    }

    update(id: number, department: Partial<ClinicalDepartment>): Observable<ClinicalDepartment> {
        return this.http.put<ApiResponse<ClinicalDepartment>>(`${this.url}/${id}`, department).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }
}
