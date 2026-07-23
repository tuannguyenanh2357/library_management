import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { BookCopyResponse, BookCopyCreationRequest, BookCopyUpdateRequest } from '../models/book-copy.model';
import { environment } from '../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class BookCopyService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/book-copies`;

  getAllBookCopies(): Observable<BookCopyResponse[]> {
    return this.http.get<BookCopyResponse[]>(this.apiUrl);
  }

  getBookCopyById(id: number): Observable<BookCopyResponse> {
    return this.http.get<BookCopyResponse>(`${this.apiUrl}/${id}`);
  }

  getBookCopyByBarcode(barcode: string): Observable<BookCopyResponse> {
    return this.http.get<BookCopyResponse>(`${this.apiUrl}/barcode/${barcode}`);
  }

  createBookCopy(request: BookCopyCreationRequest): Observable<BookCopyResponse> {
    return this.http.post<BookCopyResponse>(this.apiUrl, request);
  }

  updateBookCopy(id: number, request: BookCopyUpdateRequest): Observable<BookCopyResponse> {
    return this.http.put<BookCopyResponse>(`${this.apiUrl}/${id}`, request);
  }

  deleteBookCopy(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
