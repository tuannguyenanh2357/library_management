import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { FineResponse } from '../models/fine.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class FineService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/fines`;

  getAllFines(): Observable<FineResponse[]> {
    return this.http.get<FineResponse[]>(this.apiUrl);
  }

  getFineById(id: number): Observable<FineResponse> {
    return this.http.get<FineResponse>(`${this.apiUrl}/${id}`);
  }

  getFinesByMemberId(memberId: number): Observable<FineResponse[]> {
    return this.http.get<FineResponse[]>(`${this.apiUrl}/member/${memberId}`);
  }

  getUnpaidFines(): Observable<FineResponse[]> {
    return this.http.get<FineResponse[]>(`${this.apiUrl}/unpaid`);
  }

  payFine(fineId: number): Observable<FineResponse> {
    return this.http.put<FineResponse>(`${this.apiUrl}/${fineId}/pay`, {});
  }

  cancelFine(fineId: number, reason: string): Observable<FineResponse> {
    return this.http.put<FineResponse>(`${this.apiUrl}/${fineId}/cancel`, { reason });
  }
}
