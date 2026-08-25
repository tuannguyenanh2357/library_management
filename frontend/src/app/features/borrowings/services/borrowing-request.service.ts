import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  BorrowingRequestResponse,
  BorrowingRequestCreationRequest,
  BorrowingRequestApprovalRequest
} from '../models/borrowing-request.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BorrowingRequestService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/borrowing-requests`;

  createRequest(request: BorrowingRequestCreationRequest): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(this.apiUrl, request);
  }

  getPendingRequests(): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/pending`);
  }

  // Lấy danh sách yêu cầu đã duyệt - đang chờ độc giả đến lấy sách
  getApprovedRequests(): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/approved`);
  }

  getHistoryRequests(): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/history`);
  }

  getRequestsByMember(memberId: number): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/member/${memberId}`);
  }

  approveRequest(id: number): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/approve`, {});
  }

  issueBook(id: number): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/issue`, {});
  }

  // Hủy yêu cầu đã duyệt vì độc giả không đến lấy
  expireRequest(id: number): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/expire`, {});
  }

  rejectRequest(id: number, reason: string): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/reject?reason=${encodeURIComponent(reason)}`, {});
  }

  cancelRequest(id: number): Observable<BorrowingRequestResponse> {
    return this.http.put<BorrowingRequestResponse>(`${this.apiUrl}/${id}/cancel`, {});
  }
}
