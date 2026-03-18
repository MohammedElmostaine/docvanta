import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse } from '../models/auth.models';
import { MedicalAct, MedicalActRequest } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class MedicalActService {
    private readonly url = `${API_BASE}/medical-acts`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<MedicalAct[]> {
        return this.http.get<ApiResponse<MedicalAct[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<MedicalAct> {
        return this.http.get<ApiResponse<MedicalAct>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByClinic(clinicId: number): Observable<MedicalAct[]> {
        return this.http.get<ApiResponse<MedicalAct[]>>(`${this.url}/clinic/${clinicId}`).pipe(map(r => r.data));
    }

    getByCategory(category: string): Observable<MedicalAct[]> {
        return this.http.get<ApiResponse<MedicalAct[]>>(`${this.url}/category/${category}`).pipe(map(r => r.data));
    }

    search(query: string): Observable<MedicalAct[]> {
        return this.http.get<ApiResponse<MedicalAct[]>>(`${this.url}/search?q=${encodeURIComponent(query)}`).pipe(map(r => r.data));
    }

    create(req: MedicalActRequest): Observable<MedicalAct> {
        return this.http.post<ApiResponse<MedicalAct>>(this.url, req).pipe(map(r => r.data));
    }

    update(id: number, req: MedicalActRequest): Observable<MedicalAct> {
        return this.http.put<ApiResponse<MedicalAct>>(`${this.url}/${id}`, req).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<ApiResponse<void>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }
}
