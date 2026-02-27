import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE } from '../config/api.config';
import { ClinicPersonnel, ApiResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class ClinicPersonnelService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${API_BASE}/personnel`;

  getAll(): Observable<ClinicPersonnel[]> {
    return this.http.get<ApiResponse<ClinicPersonnel[]>>(this.API_URL).pipe(
      map(response => response.data)
    );
  }

  getById(id: number): Observable<ClinicPersonnel> {
    return this.http.get<ApiResponse<ClinicPersonnel>>(`${this.API_URL}/${id}`).pipe(
      map(response => response.data)
    );
  }

  getByUsername(username: string): Observable<ClinicPersonnel> {
    return this.http.get<ApiResponse<ClinicPersonnel>>(`${this.API_URL}/username/${username}`).pipe(
      map(response => response.data)
    );
  }

  getByClinic(clinicId: number): Observable<ClinicPersonnel[]> {
    return this.http.get<ApiResponse<ClinicPersonnel[]>>(`${this.API_URL}/clinic/${clinicId}`).pipe(
      map(response => response.data)
    );
  }

  getByType(personnelType: string): Observable<ClinicPersonnel[]> {
    return this.http.get<ApiResponse<ClinicPersonnel[]>>(`${this.API_URL}/type/${personnelType}`).pipe(
      map(response => response.data)
    );
  }

  update(id: number, personnel: Partial<ClinicPersonnel>): Observable<ClinicPersonnel> {
    return this.http.put<ApiResponse<ClinicPersonnel>>(`${this.API_URL}/${id}`, personnel).pipe(
      map(response => response.data)
    );
  }

  updateType(id: number, personnelType: string): Observable<ClinicPersonnel> {
    return this.http.put<ApiResponse<ClinicPersonnel>>(`${this.API_URL}/${id}/type?personnelType=${personnelType}`, {}).pipe(
      map(response => response.data)
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`).pipe(
      map(() => undefined)
    );
  }

}
