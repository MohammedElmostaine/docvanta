import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse } from '../models/auth.models';
import { Invoice, InvoiceRequest, InvoiceLineRequest, DailySummary } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class InvoiceService {
    private readonly url = `${API_BASE}/invoices`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<Invoice[]> {
        return this.http.get<ApiResponse<Invoice[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<Invoice> {
        return this.http.get<ApiResponse<Invoice>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByPatient(patientId: number): Observable<Invoice[]> {
        return this.http.get<ApiResponse<Invoice[]>>(`${this.url}/patient/${patientId}`).pipe(map(r => r.data));
    }

    getByAppointment(appointmentId: number): Observable<Invoice | null> {
        return this.http.get<ApiResponse<Invoice>>(`${this.url}/appointment/${appointmentId}`).pipe(map(r => r.data));
    }

    getByStatus(status: string): Observable<Invoice[]> {
        return this.http.get<ApiResponse<Invoice[]>>(`${this.url}/status/${status}`).pipe(map(r => r.data));
    }

    getUnpaid(clinicId: number): Observable<Invoice[]> {
        return this.http.get<ApiResponse<Invoice[]>>(`${this.url}/unpaid?clinicId=${clinicId}`).pipe(map(r => r.data));
    }

    getDailySummary(clinicId: number, date: string): Observable<DailySummary> {
        return this.http.get<ApiResponse<DailySummary>>(`${this.url}/daily-summary?clinicId=${clinicId}&date=${date}`).pipe(map(r => r.data));
    }

    create(req: InvoiceRequest): Observable<Invoice> {
        return this.http.post<ApiResponse<Invoice>>(this.url, req).pipe(map(r => r.data));
    }

    addLine(invoiceId: number, line: InvoiceLineRequest): Observable<Invoice> {
        return this.http.post<ApiResponse<Invoice>>(`${this.url}/${invoiceId}/lines`, line).pipe(map(r => r.data));
    }

    removeLine(invoiceId: number, lineId: number): Observable<Invoice> {
        return this.http.delete<ApiResponse<Invoice>>(`${this.url}/${invoiceId}/lines/${lineId}`).pipe(map(r => r.data));
    }

    finalize(id: number): Observable<Invoice> {
        return this.http.patch<ApiResponse<Invoice>>(`${this.url}/${id}/finalize`, {}).pipe(map(r => r.data));
    }

    cancel(id: number): Observable<Invoice> {
        return this.http.patch<ApiResponse<Invoice>>(`${this.url}/${id}/cancel`, {}).pipe(map(r => r.data));
    }

    generateFromAppointment(appointmentId: number): Observable<Invoice> {
        return this.http.post<ApiResponse<Invoice>>(`${this.url}/generate/${appointmentId}`, {}).pipe(map(r => r.data));
    }
}
