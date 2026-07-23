import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BorrowingResponse, BorrowingCreationRequest } from '../../../core/models/borrowing.model';
import { environment } from '../../../../environments/environment';

export interface OverdueBookProjection {
  borrowingId: number;
  memberId: number;
  memberName: string;
  memberPhone: string;
  bookTitle: string;
  barCode: string;
  borrowDate: string;
  dueDate: string;
  overdueDays: number;
}

@Injectable({
  providedIn: 'root'
})
export class BorrowingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/borrowings`;

  getOverdueBooksFromSP(): Observable<OverdueBookProjection[]> {
    return this.http.get<OverdueBookProjection[]>(`${this.apiUrl}/sp-overdue`);
  }

  getBorrowingsByMemberId(memberId: number): Observable<BorrowingResponse[]> {
    return this.http.get<BorrowingResponse[]>(`${this.apiUrl}/member/${memberId}`);
  }

  getAllBorrowings(): Observable<BorrowingResponse[]> {
    return this.http.get<BorrowingResponse[]>(this.apiUrl);
  }

  getBorrowingsByCopyId(copyId: number): Observable<BorrowingResponse[]> {
    return this.http.get<BorrowingResponse[]>(`${this.apiUrl}/copy/${copyId}`);
  }

  borrowBook(request: BorrowingCreationRequest): Observable<BorrowingResponse> {
    return this.http.post<BorrowingResponse>(this.apiUrl, request);
  }

  returnBook(borrowingId: number): Observable<BorrowingResponse> {
    return this.http.put<BorrowingResponse>(`${this.apiUrl}/${borrowingId}/return`, {});
  }

  renewBorrowing(borrowingId: number): Observable<BorrowingResponse> {
    return this.http.put<BorrowingResponse>(`${this.apiUrl}/${borrowingId}/renew`, {});
  }

  reportLost(borrowingId: number): Observable<BorrowingResponse> {
    return this.http.put<BorrowingResponse>(`${this.apiUrl}/${borrowingId}/report-lost`, {});
  }

  reportDamaged(borrowingId: number): Observable<BorrowingResponse> {
    return this.http.put<BorrowingResponse>(`${this.apiUrl}/${borrowingId}/report-damaged`, {});
  }
}
