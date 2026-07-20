import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BorrowingResponse, BorrowingCreationRequest } from '../../../core/models/borrowing.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BorrowingService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/borrowings`;

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
}
