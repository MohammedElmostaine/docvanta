import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, map } from 'rxjs';
import { API_BASE } from '../config/api.config';
import { ApiResponse } from '../models/auth.models';
import { PaymentRecord, PaymentRequest } from '../models/billing.models';

@Injectable({ providedIn: 'root' })
export class PaymentService {
    private readonly url = `${API_BASE}/payments`;

    constructor(private http: HttpClient) {}

    getByInvoice(invoiceId: number): Observable<PaymentRecord[]> {
        return this.http.get<ApiResponse<PaymentRecord[]>>(`${this.url}/invoice/${invoiceId}`).pipe(map(r => r.data));
    }

    record(req: PaymentRequest): Observable<PaymentRecord> {
        return this.http.post<ApiResponse<PaymentRecord>>(this.url, req).pipe(map(r => r.data));
    }
}
