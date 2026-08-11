import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../../../environments/environment';
import { Member } from '@core/models/member.model';

// ─── Models dùng cho quản trị member (admin) ──────────────────────────────────

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
}

