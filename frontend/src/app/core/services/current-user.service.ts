import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { Member, ProfileUpdateRequest, ChangePasswordRequest } from '../models/member.model';
import { BooksResponse } from '../../shared/models/book.model';
import { environment } from '../../../environments/environment';

import { AuthService } from './auth.service';

/** Thông tin & thao tác của user đang đăng nhập (khác với quản trị member ở features/members) */
@Injectable({
  providedIn: 'root'
})
export class CurrentUserService {
  private http = inject(HttpClient);
  private authService = inject(AuthService);
  private apiUrl = `${environment.apiUrl}/members`;

  public currentUserProfile = signal<Member | null>(null);
  public favoriteBooks = signal<BooksResponse[]>([]);

  /** Lấy thông tin profile của user đang đăng nhập (/members/me) */
  getMyProfile(): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/me`).pipe(
      tap(member => {
        this.currentUserProfile.set(member);
        if (member && member.id) {
          this.authService.setMemberId(member.id);
        }
      })
    );
  }

  /** Cập nhật thông tin cá nhân (/members/me) */
  updateMyProfile(request: ProfileUpdateRequest): Observable<Member> {
    return this.http.put<Member>(`${this.apiUrl}/me`, request).pipe(
      tap(member => this.currentUserProfile.set(member))
    );
  }

  /** Đổi mật khẩu (/members/me/password) */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me/password`, request);
  }

  /** Xóa thông tin cache profile khi logout */
  clearUserProfile(): void {
    this.currentUserProfile.set(null);
    this.favoriteBooks.set([]);
  }

  /** Lấy danh sách sách yêu thích */
  getFavoriteBooks(): Observable<BooksResponse[]> {
    return this.http.get<BooksResponse[]>(`${this.apiUrl}/me/favorites`).pipe(
      tap(books => this.favoriteBooks.set(books))
    );
  }

  /** Thêm sách vào yêu thích */
  addFavoriteBook(bookId: number): Observable<any> {
    return this.http.post(`${this.apiUrl}/me/favorites/${bookId}`, {}).pipe(
      tap(() => {
        // Tải lại danh sách sau khi thêm thành công
        this.getFavoriteBooks().subscribe();
      })
    );
  }

  /** Xóa sách khỏi yêu thích */
  removeFavoriteBook(bookId: number): Observable<any> {
    return this.http.delete(`${this.apiUrl}/me/favorites/${bookId}`).pipe(
      tap(() => {
        // Cập nhật lại danh sách local
        this.favoriteBooks.update(books => books.filter(b => b.id !== bookId));
      })
    );
  }

}
