import { Injectable, inject, signal, PLATFORM_ID } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { isPlatformBrowser } from '@angular/common';
import { Observable, tap } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest } from '../models/auth.model';
import { environment } from '../../../environments/environment';

interface AuthState {
  username: string | null;
  role: string | null;
  memberId: number | null;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private platformId = inject(PLATFORM_ID);
  private apiUrl = `${environment.apiUrl}/auth`;

  private state = signal<AuthState>(this.readStateFromStorage());

  private readStateFromStorage(): AuthState {
    if (isPlatformBrowser(this.platformId)) {
      const memberId = localStorage.getItem('auth_memberId');
      return {
        username: localStorage.getItem('auth_username'),
        role: localStorage.getItem('auth_role'),
        memberId: memberId ? parseInt(memberId, 10) : null
      };
    }
    return { username: null, role: null, memberId: null };
  }

  // đăng nhập /auth/login
  login(request: AuthenticationRequest): Observable<AuthenticationResponse> {
    return this.http.post<AuthenticationResponse>(`${this.apiUrl}/login`, request, { withCredentials: true }).pipe(
      tap(response => {
        if (response && response.authenticated) {
          this.saveUserDetails(response);
        }
      })
    );
  }

  // đăng ký calling /auth/register
  register(request: RegisterRequest): Observable<any> {
    return this.http.post<any>(`${this.apiUrl}/register`, request);
  }

  // quản lý thông tin phiên làm việc
  saveUserDetails(response: AuthenticationResponse): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.setItem('auth_username', response.username);
      localStorage.setItem('auth_role', response.role);
      localStorage.setItem('auth_memberId', response.memberId.toString());
    }
    this.state.set({ username: response.username, role: response.role, memberId: response.memberId });
  }

  clearUserDetails(): void {
    if (isPlatformBrowser(this.platformId)) {
      localStorage.removeItem('auth_username');
      localStorage.removeItem('auth_role');
      localStorage.removeItem('auth_memberId');
    }
    this.state.set({ username: null, role: null, memberId: null });
  }

  isLoggedIn(): boolean {
    return !!this.state().username;
  }

  // Lấy quyền user
  getUserRole(): string | null {
    return this.state().role;
  }

  // Lấy username
  getUsername(): string | null {
    return this.state().username;
  }

  // Lấy memberId
  getCurrentUserId(): number | null {
    return this.state().memberId;
  }

  // Kiểm tra quyền Admin hoặc Librarian (Dùng chung cho các tác vụ quản lý cơ bản)
  isAdmin(): boolean {
    const role = this.getUserRole();
    return role === 'ADMIN' || role === 'LIBRARIAN';
  }

  // Chỉ kiểm tra quyền Admin (Dùng cho các tác vụ nhạy cảm như xóa tài khoản, quản lý thủ thư)
  isStrictAdmin(): boolean {
    return this.getUserRole() === 'ADMIN';
  }

  // Chỉ kiểm tra quyền Thủ thư
  isLibrarian(): boolean {
    return this.getUserRole() === 'LIBRARIAN';
  }

  logout(): void {
    // Gọi API logout để xoá HttpOnly cookie trên server, kèm withCredentials
    this.http.post(`${this.apiUrl}/logout`, {}, { withCredentials: true }).subscribe({
      next: () => this.clearUserDetails(),
      error: () => this.clearUserDetails()
    });
  }
}
