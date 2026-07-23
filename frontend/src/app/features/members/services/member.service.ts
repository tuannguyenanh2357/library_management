import { Injectable, inject, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { environment } from '../../../../environments/environment';

// ─── Models dùng chung cho Member ─────────────────────────────────────────────

/** Dữ liệu trả về khi lấy thông tin member (dùng cho profile + admin) */
export interface Member {
  id: number;
  name: string;
  username: string;
  memberCode: string;
  email: string;
  phone: string;
  address: string;
  isActive: boolean;
  avatar?: string;
  age?: number;
  role: string;
}

/** Request tạo mới member (dùng ở admin) */
export interface MemberCreationRequest {
  name: string;
  email: string;
  username: string;
  password?: string;
  phone: string;
  address: string;
  age: number;
}

/** Request cập nhật thông tin cá nhân của member (dùng ở profile) */
export interface ProfileUpdateRequest {
  name?: string;
  email?: string;
  phone?: string;
  address?: string;
  avatar?: string;
}

/** Request cập nhật member do admin thực hiện (có thêm username, isActive, age) */
export interface MemberUpdateRequest {
  name: string;
  email: string;
  username: string;
  password?: string;
  phone: string;
  address: string;
  age?: number;
  isActive?: boolean;
}

/** Request đổi mật khẩu */
export interface ChangePasswordRequest {
  oldPassword?: string;
  newPassword?: string;
}

export interface UnpaidMemberProjection {
  memberId: number;
  memberCode: string;
  name: string;
  email: string;
  phone: string;
  totalUnpaidAmount: number;
  unpaidFinesCount: number;
}

// ─── Service ──────────────────────────────────────────────────────────────────

@Injectable({
  providedIn: 'root'
})
export class MemberService {
  private http = inject(HttpClient);
  private apiUrl = `${environment.apiUrl}/members`;

  public currentUserProfile = signal<Member | null>(null);

  // ── Admin: Quản lý toàn bộ member ──────────────────────────────────────────

  getAllMembers(): Observable<Member[]> {
    return this.http.get<Member[]>(this.apiUrl);
  }

  getMembersWithUnpaidFines(): Observable<UnpaidMemberProjection[]> {
    return this.http.get<UnpaidMemberProjection[]>(`${this.apiUrl}/unpaid-fines`);
  }

  getMemberById(id: number): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/${id}`);
  }

  createMember(data: MemberCreationRequest): Observable<Member> {
    return this.http.post<Member>(this.apiUrl, data);
  }

  updateMember(id: number, data: any): Observable<Member> {
    return this.http.put<Member>(`${this.apiUrl}/${id}`, data);
  }

  deleteMember(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }

  // ── Profile: Thông tin cá nhân của member đang đăng nhập ───────────────────

  /** Lấy thông tin profile của member đang đăng nhập (/members/me) */
  getMyProfile(): Observable<Member> {
    return this.http.get<Member>(`${this.apiUrl}/me`).pipe(
      tap(member => this.currentUserProfile.set(member))
    );
  }

  /** Cập nhật thông tin cá nhân (/members/me) */
  updateMyProfile(request: ProfileUpdateRequest): Observable<Member> {
    return this.http.put<Member>(`${this.apiUrl}/me`, request).pipe(
      tap(member => this.currentUserProfile.set(member))
    );
  }

  /** Xóa thông tin cache profile khi logout */
  clearUserProfile(): void {
    this.currentUserProfile.set(null);
  }

  /** Đổi mật khẩu (/members/me/password) */
  changePassword(request: ChangePasswordRequest): Observable<void> {
    return this.http.put<void>(`${this.apiUrl}/me/password`, request);
  }
}

