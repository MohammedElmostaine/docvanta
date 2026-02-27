import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';
import { API_BASE } from '../config/api.config';
import { Practitioner, ApiResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class PractitionerService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = `${API_BASE}/practitioners`;

  getAll(): Observable<Practitioner[]> {
    return this.http.get<ApiResponse<Practitioner[]>>(this.API_URL).pipe(
      map(response => response.data)
    );
  }

  getById(id: number): Observable<Practitioner> {
    return this.http.get<ApiResponse<Practitioner>>(`${this.API_URL}/${id}`).pipe(
      map(response => response.data)
    );
  }

  getByUsername(username: string): Observable<Practitioner> {
    return this.http.get<ApiResponse<Practitioner>>(`${this.API_URL}/username/${username}`).pipe(
      map(response => response.data)
    );
  }

  search(q: string): Observable<Practitioner[]> {
    return this.http.get<ApiResponse<Practitioner[]>>(`${this.API_URL}/search?q=${encodeURIComponent(q)}`).pipe(
      map(response => response.data)
    );
  }

  getByClinic(clinicId: number): Observable<Practitioner[]> {
    return this.http.get<ApiResponse<Practitioner[]>>(`${this.API_URL}/clinic/${clinicId}`).pipe(
      map(response => response.data)
    );
  }

  getByDepartment(departmentId: number): Observable<Practitioner[]> {
    return this.http.get<ApiResponse<Practitioner[]>>(`${this.API_URL}/department/${departmentId}`).pipe(
      map(response => response.data)
    );
  }

  getBySpecialty(specialtyName: string): Observable<Practitioner[]> {
    return this.http.get<ApiResponse<Practitioner[]>>(`${this.API_URL}/specialty/${encodeURIComponent(specialtyName)}`).pipe(
      map(response => response.data)
    );
  }

  update(id: number, practitioner: Partial<Practitioner>): Observable<Practitioner> {
    return this.http.put<ApiResponse<Practitioner>>(`${this.API_URL}/${id}`, practitioner).pipe(
      map(response => response.data)
    );
  }

  delete(id: number): Observable<void> {
    return this.http.delete<ApiResponse<void>>(`${this.API_URL}/${id}`).pipe(
      map(() => undefined)
    );
  }
}
