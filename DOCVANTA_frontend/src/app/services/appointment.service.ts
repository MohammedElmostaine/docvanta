import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse, Appointment, AppointmentRequest, AppointmentBySpecialtyRequest } from '../models/auth.models';
import { PerformedAct, PerformedActRequest, PriceEstimate, TimeSlot } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class AppointmentService {
    private readonly url = `${API_BASE}/appointments`;

    constructor(private http: HttpClient) {}

    getAll(): Observable<Appointment[]> {
        return this.http.get<ApiResponse<Appointment[]>>(this.url).pipe(map(r => r.data));
    }

    getById(id: number): Observable<Appointment> {
        return this.http.get<ApiResponse<Appointment>>(`${this.url}/${id}`).pipe(map(r => r.data));
    }

    getByPractitioner(practitionerId: number): Observable<Appointment[]> {
        return this.http.get<ApiResponse<Appointment[]>>(`${this.url}/practitioner/${practitionerId}`).pipe(map(r => r.data));
    }

    getByPatient(patientId: number): Observable<Appointment[]> {
        return this.http.get<ApiResponse<Appointment[]>>(`${this.url}/patient/${patientId}`).pipe(map(r => r.data));
    }

    getByStatus(status: string): Observable<Appointment[]> {
        return this.http.get<ApiResponse<Appointment[]>>(`${this.url}/status/${status}`).pipe(map(r => r.data));
    }

    getByDateRange(start: string, end: string): Observable<Appointment[]> {
        return this.http.get<ApiResponse<Appointment[]>>(`${this.url}/date-range?start=${start}&end=${end}`).pipe(map(r => r.data));
    }

    create(req: AppointmentRequest): Observable<Appointment> {
        return this.http.post<ApiResponse<Appointment>>(this.url, req).pipe(map(r => r.data));
    }

    createBySpecialty(req: AppointmentBySpecialtyRequest): Observable<Appointment> {
        return this.http.post<ApiResponse<Appointment>>(`${this.url}/by-specialty`, req).pipe(map(r => r.data));
    }

    update(id: number, req: AppointmentRequest): Observable<Appointment> {
        return this.http.put<ApiResponse<Appointment>>(`${this.url}/${id}`, req).pipe(map(r => r.data));
    }

    updateStatus(id: number, status: string): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/status?status=${status}`, {}).pipe(map(r => r.data));
    }

    approve(id: number): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/approve`, {}).pipe(map(r => r.data));
    }

    reject(id: number): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/reject`, {}).pipe(map(r => r.data));
    }

    confirm(id: number): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/confirm`, {}).pipe(map(r => r.data));
    }

    complete(id: number): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/complete`, {}).pipe(map(r => r.data));
    }

    cancel(id: number): Observable<Appointment> {
        return this.http.patch<ApiResponse<Appointment>>(`${this.url}/${id}/cancel`, {}).pipe(map(r => r.data));
    }

    delete(id: number): Observable<void> {
        return this.http.delete<void>(`${this.url}/${id}`);
    }

    // ── Price Estimation ──
    estimatePrice(practitionerId: number, specialtyId?: number): Observable<PriceEstimate> {
        let params = `practitionerId=${practitionerId}`;
        if (specialtyId) params += `&specialtyId=${specialtyId}`;
        return this.http.get<ApiResponse<PriceEstimate>>(`${this.url}/estimate-price?${params}`).pipe(map(r => r.data));
    }

    // ── Performed Acts ──
    getPerformedActs(appointmentId: number): Observable<PerformedAct[]> {
        return this.http.get<ApiResponse<PerformedAct[]>>(`${this.url}/${appointmentId}/performed-acts`).pipe(map(r => r.data));
    }

    addPerformedAct(appointmentId: number, req: PerformedActRequest): Observable<PerformedAct> {
        return this.http.post<ApiResponse<PerformedAct>>(`${this.url}/${appointmentId}/performed-acts`, req).pipe(map(r => r.data));
    }

    removePerformedAct(appointmentId: number, actId: number): Observable<void> {
        return this.http.delete<ApiResponse<void>>(`${this.url}/${appointmentId}/performed-acts/${actId}`).pipe(map(r => r.data));
    }

    // ── Scheduling / Availability ──
    getAvailableSlots(practitionerId: number, date: string): Observable<TimeSlot[]> {
        return this.http.get<ApiResponse<TimeSlot[]>>(`${this.url}/available-slots?practitionerId=${practitionerId}&date=${date}`).pipe(map(r => r.data));
    }
}
