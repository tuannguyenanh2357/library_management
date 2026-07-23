import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { 
  BorrowingRequestResponse, 
  BorrowingRequestCreationRequest,
  BorrowingRequestApprovalRequest
} from '../models/borrowing-request.model';
import { environment } from '../../../environments/environment';

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

  getHistoryRequests(): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/history`);
  }

  getRequestsByMember(memberId: number): Observable<BorrowingRequestResponse[]> {
    return this.http.get<BorrowingRequestResponse[]>(`${this.apiUrl}/member/${memberId}`);
  }

  approveRequest(id: number): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/approve`, {});
  }

  rejectRequest(id: number, reason: string): Observable<BorrowingRequestResponse> {
    return this.http.post<BorrowingRequestResponse>(`${this.apiUrl}/${id}/reject?reason=${encodeURIComponent(reason)}`, {});
  }

  cancelRequest(id: number): Observable<BorrowingRequestResponse> {
    return this.http.put<BorrowingRequestResponse>(`${this.apiUrl}/${id}/cancel`, {});
  }
}
