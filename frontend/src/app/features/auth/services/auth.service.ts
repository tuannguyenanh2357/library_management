import { Injectable, inject, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../models/auth.model';
import { environment } from '../../../../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private apiUrl = `${environment.apiUrl}/auth`;

  // đăng nhập /auth/login
  login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/login`, request).pipe(
      tap(response => {
        if (response && response.token) {
          this.saveToken(response.token);
        }
      })
    );
  }

  // đăng ký calling /auth/register
  register(request: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, request);
  }

  // quản lý token
  saveToken(token: string): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('auth_token', token);
    }
  }

  getToken(): string | null {
    if (isPlatformBrowser(this.platformId)) {
      return localStorage.getItem('auth_token');
    }
    return null;
  }

  removeToken(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('auth_token');
    }
  }

  isLoggedIn(): boolean {
    return !!this.getToken();
  }

  // Lấy thông tin user
  private getDecodedToken(): any {
    const token = this.getToken();
    if (!token) return null;
    try {
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = atob(payloadBase64);
      return JSON.parse(decodedPayload);
    } catch (e) {
      console.error('Failed to decode token', e);
      return null;
    }
  }

  // Lấy quyền user
  getUserRole(): string | null {
    const decoded = this.getDecodedToken();
    return decoded ? decoded.scope : null;
  }

  // Lấy username
  getUsername(): string | null {
    const decoded = this.getDecodedToken();
    return decoded ? decoded.sub : null;
  }

  // Lấy memberId
  getCurrentUserId(): number | null {
    const decoded = this.getDecodedToken();
    return decoded ? decoded.memberId : null;
  }

  // Kiểm tra quyền Admin hoặc Librarian
  isAdmin(): boolean {
    const role = this.getUserRole();
    return role === 'ADMIN' || role === 'LIBRARIAN';
  }

  logout(): void {
    this.removeToken();
  }
}
