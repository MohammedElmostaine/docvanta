import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse, MedicalDocument, MedicalDocumentRequest } from '../models/auth.models';

@Injectable({ providedIn: 'root' })
export class DocumentService {
    private readonly url = `${API_BASE}/medical-documents`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<MedicalDocument[]> {
        return this.http.get<ApiResponse<MedicalDocument[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<MedicalDocument> {
        return this.http.get<ApiResponse<MedicalDocument>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByPatient(patientId: number): Observable<MedicalDocument[]> {
        return this.http.get<ApiResponse<MedicalDocument[]>>(`${this.url}/patient/${patientId}`).pipe(map(r => r.data));
    }

    getByPractitioner(practitionerId: number): Observable<MedicalDocument[]> {
        return this.http.get<ApiResponse<MedicalDocument[]>>(`${this.url}/practitioner/${practitionerId}`).pipe(map(r => r.data));
    }

    getAuthorizedForPatient(patientId: number): Observable<MedicalDocument[]> {
        return this.http.get<ApiResponse<MedicalDocument[]>>(`${this.url}/patient/${patientId}/authorized`).pipe(map(r => r.data));
    }

    getByType(type: string): Observable<MedicalDocument[]> {
        return this.http.get<ApiResponse<MedicalDocument[]>>(`${this.url}/type/${type}`).pipe(map(r => r.data));
    }

    create(req: MedicalDocumentRequest): Observable<MedicalDocument> {
        return this.http.post<ApiResponse<MedicalDocument>>(this.url, req).pipe(map(r => r.data));
    }

    update(id: number, req: MedicalDocumentRequest): Observable<MedicalDocument> {
        return this.http.put<ApiResponse<MedicalDocument>>(`${this.url}/${id}`, req).pipe(map(r => r.data));
    }

    authorize(id: number, authorized: boolean): Observable<MedicalDocument> {
        return this.http.patch<ApiResponse<MedicalDocument>>(`${this.url}/${id}/authorize?authorized=${authorized}`, {}).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }
}
