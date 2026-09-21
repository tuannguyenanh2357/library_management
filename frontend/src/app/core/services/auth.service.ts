import { Injectable, inject, signal, computed } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap, catchError, of } from 'rxjs';
import { AuthenticationRequest, AuthenticationResponse, RegisterRequest, UserResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

interface AuthState {
  username: string | null;
  role: string | null;
  memberId: number | null;
  initialized: boolean;
}

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/auth`;

  private state = signal<AuthState>({
    username: null,
    role: null,
    memberId: null,
    initialized: false
  });

  readonly currentUser = computed(() => this.state());
  readonly isInitialized = computed(() => this.state().initialized);

  // Khôi phục phiên làm việc từ Cookie bằng API /auth/me
  fetchCurrentUser(): Observable<UserResponse | null> {
    return this.http.get<UserResponse>(`${this.apiUrl}/me`, { withCredentials: true }).pipe(
      tap({
        next: (user) => {
          this.state.set({
            username: user.username,
            role: user.role,
            memberId: user.id,
            initialized: true
          });
        },
        error: () => {
          this.clearUserDetails();
        }
      }),
      catchError(() => {
        this.clearUserDetails();
        return of(null);
      })
    );
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

  saveUserDetails(response: AuthenticationResponse): void {
    this.state.set({
      username: response.username,
      role: response.role,
      memberId: response.memberId,
      initialized: true
    });
  }

  clearUserDetails(): void {
    this.state.set({ username: null, role: null, memberId: null, initialized: true });
  }

  setMemberId(memberId: number): void {
    this.state.update(s => ({ ...s, memberId }));
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
