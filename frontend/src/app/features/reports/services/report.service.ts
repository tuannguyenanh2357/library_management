import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { WeeklyRevenueReport } from '../models/report.model';
import { environment } from '../../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ReportService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/reports`;

  getRevenueReport(from?: string, to?: string): Observable<WeeklyRevenueReport> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get<WeeklyRevenueReport>(`${this.apiUrl}/revenue`, { params });
  }

  exportRevenuePdf(from?: string, to?: string): Observable<Blob> {
    let params = new HttpParams();
    if (from) params = params.set('from', from);
    if (to)   params = params.set('to', to);
    return this.http.get(`${this.apiUrl}/revenue/export`, { params, responseType: 'blob' });
  }
}
