import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MemberService, MemberCreationRequest, MemberUpdateRequest, UnpaidMemberProjection } from '../../services/member.service';
import { Member } from '@core/models/member.model';

import { BorrowingService } from '../../../borrowings/services/borrowing.service';
import { BorrowingResponse } from '../../../borrowings/models/borrowing.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { ConfirmService } from '../../../../shared/services/confirm.service';
import { AuthService } from '@core/services/auth.service';

import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-admin-member-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, PaginationComponent],
  templateUrl: './admin-member-list.component.html',
  styleUrls: ['./admin-member-list.component.css']
})
export class AdminMemberListComponent implements OnInit {
  private memberService = inject(MemberService);
  private borrowingService = inject(BorrowingService);
  private toastService = inject(ToastService);
  private confirmService = inject(ConfirmService);
  protected authService = inject(AuthService);

  Math = Math;

  members = signal<Member[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');

  // Pagination state
  currentPage = signal<number>(1);
  pageSize = 10;
  totalPages = computed(() => Math.ceil(this.members().length / this.pageSize) || 1);
  paginatedMembers = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.members().slice(start, start + this.pageSize);
  });

  goToPage(page: number): void {
    if (page >= 1 && page <= this.totalPages()) {
      this.currentPage.set(page);
    }
  }

  pagesArray = computed(() => Array.from({ length: this.totalPages() }, (_, i) => i + 1));

  // Modals state
  isMemberModalOpen = signal<boolean>(false);
  isHistoryModalOpen = signal<boolean>(false);
  isUnpaidFinesModalOpen = signal<boolean>(false);
  unpaidMembers = signal<UnpaidMemberProjection[]>([]);
  isUnpaidLoading = signal<boolean>(false);
  isEditMode = signal<boolean>(false);

  openUnpaidFinesModal(): void {
    this.isUnpaidFinesModalOpen.set(true);
    this.isUnpaidLoading.set(true);
    this.memberService.getMembersWithUnpaidFines().subscribe({
      next: (data) => {
        this.unpaidMembers.set(data);
        this.isUnpaidLoading.set(false);
      },
      error: (err) => {
        console.error('Lỗi khi tải danh sách độc giả nợ phạt:', err);
        this.toastService.error('Không thể tải danh sách nợ tiền phạt');
        this.isUnpaidLoading.set(false);
      }
    });
  }

  closeUnpaidFinesModal(): void {
    this.isUnpaidFinesModalOpen.set(false);
  }

  // Form state
  currentMember = signal<Partial<MemberCreationRequest & MemberUpdateRequest & { id?: number }>>({});

  // History state
  memberBorrowings = signal<BorrowingResponse[]>([]);
  isHistoryLoading = signal<boolean>(false);
  selectedMemberName = signal<string>('');

  ngOnInit(): void {
    this.loadMembers();
  }

  loadMembers(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.memberService.getAllMembers().subscribe({
      next: (data) => {
        const sorted = (data || []).sort((a, b) => b.id - a.id);
        this.members.set(sorted);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Lỗi khi tải danh sách độc giả:', err);
        this.errorMessage.set('Không thể kết nối với Backend');
        this.isLoading.set(false);
      }
    });
  }

  openAddModal(): void {
    this.isEditMode.set(false);
    this.currentMember.set({
      name: '', email: '', username: '', password: '', phone: '', address: '', age: 18, isActive: true
    });
    this.isMemberModalOpen.set(true);
  }

  openEditModal(member: Member): void {
    this.isEditMode.set(true);
    this.currentMember.set({
      id: member.id,
      name: member.name,
      email: member.email,
      username: member.username,
      password: '',
      phone: member.phone,
      address: member.address,
      age: member.age,
      isActive: member.isActive
    });
    this.isMemberModalOpen.set(true);
  }

  closeMemberModal(): void {
    this.isMemberModalOpen.set(false);
  }


  // thông báo chi tiết các lỗi khi thêm, cập nhật, xóa độc giả
  private extractErrorMessage(err: any, fallback: string): string {
    if (!err) return fallback;
    if (typeof err.error === 'string') return err.error;
    if (err.error?.message) return err.error.message;
    if (err.error?.details && typeof err.error.details === 'object') {
      const messages = Object.values(err.error.details).filter(msg => typeof msg === 'string');
      if (messages.length > 0) {
        return messages.join(', ');
      }
    }
    if (err.message) return err.message;
    return fallback;
  }

  saveMember(): void {
    const data = this.currentMember();

    if (!data.name?.trim()) {
      this.toastService.warning('Họ và tên không được để trống');
      return;
    }
    if (!data.email?.trim()) {
      this.toastService.warning('Email không được để trống');
      return;
    }
    if (!data.username?.trim()) {
      this.toastService.warning('Tên đăng nhập không được để trống');
      return;
    }
    if (data.name.trim().length > 100) {
      this.toastService.warning('Họ tên không được vượt quá 100 ký tự');
      return;
    }
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    if (!emailRegex.test(data.email.trim())) {
      this.toastService.warning('Định dạng Email không hợp lệ');
      return;
    }
    if (data.username.trim().length < 3 || data.username.trim().length > 50) {
      this.toastService.warning('Tên đăng nhập phải từ 3 đến 50 ký tự');
      return;
    }
    if (data.phone?.trim()) {
      const phoneRegex = /^(0[3|5|7|8|9])+([0-9]{8})$/;
      if (!phoneRegex.test(data.phone.trim())) {
        this.toastService.warning('Số điện thoại không đúng định dạng VN (VD: 0912345678)');
        return;
      }
    }
    if (data.address?.trim() && data.address.trim().length > 255) {
      this.toastService.warning('Địa chỉ không được vượt quá 255 ký tự');
      return;
    }
    if (data.age !== undefined && data.age !== null && data.age < 1) {
      this.toastService.warning('Tuổi phải lớn hơn 0');
      return;
    }

    if (this.isEditMode()) {
      const updateReq: MemberUpdateRequest = {
        name: data.name!,
        email: data.email!,
        username: data.username!,
        password: data.password || undefined,
        phone: data.phone!,
        address: data.address!,
        age: data.age!,
        isActive: data.isActive
      };

      this.memberService.updateMember(data.id!, updateReq).subscribe({
        next: () => {
          this.toastService.success('Cập nhật độc giả thành công!');
          this.closeMemberModal();
          this.loadMembers();
        },
        error: (err) => {
          console.error(err);
          this.toastService.error(this.extractErrorMessage(err, 'Lỗi cập nhật độc giả!'));
        }
      });
    } else {
      if (!data.password) {
        this.toastService.warning('Mật khẩu không được để trống');
        return;
      }
      if (data.password.length < 8) {
        this.toastService.warning('Mật khẩu phải có ít nhất 8 ký tự');
        return;
      }
      const createReq: MemberCreationRequest = {
        name: data.name!,
        email: data.email!,
        username: data.username!,
        password: data.password,
        phone: data.phone!,
        address: data.address!,
        age: data.age!
      };
      this.memberService.createMember(createReq).subscribe({
        next: () => {
          this.toastService.success('Thêm độc giả thành công!');
          this.closeMemberModal();
          this.loadMembers();
        },
        error: (err) => {
          console.error(err);
          this.toastService.error(this.extractErrorMessage(err, 'Lỗi thêm độc giả!'));
        }
      });
    }
  }

  async deleteMember(id: number): Promise<void> {
    const member = this.members().find(m => m.id === id);
    if (member?.role === 'ADMIN') {
      this.toastService.warning('Không thể xóa tài khoản Quản trị viên (ADMIN).');
      return;
    }

    if (await this.confirmService.confirm('Bạn có chắc chắn muốn xóa độc giả này không? Tất cả dữ liệu liên quan sẽ bị mất.')) {
      this.memberService.deleteMember(id).subscribe({
        next: () => {
          this.toastService.success('Xóa thành công!');
          this.loadMembers();
        },
        error: (err) => {
          console.error(err);
          if (err.status === 403) {
            this.toastService.error('Bạn không có quyền xóa độc giả! Chỉ ADMIN mới được phép.');
          } else {
            this.toastService.error(this.extractErrorMessage(err, 'Không thể xóa độc giả này. Đã xảy ra lỗi.'));
          }
        }
      });
    }
  }

  async toggleLock(member: Member): Promise<void> {
    const newStatus = !member.isActive;
    const action = newStatus ? 'mở khóa' : 'khóa';
    if (await this.confirmService.confirm(`Bạn có chắc muốn ${action} tài khoản của ${member.name}?`)) {
      const updateReq: MemberUpdateRequest = {
        name: member.name,
        email: member.email,
        username: member.username,
        phone: member.phone,
        address: member.address,
        age: member.age,
        isActive: newStatus
      };

      this.memberService.updateMember(member.id, updateReq).subscribe({
        next: () => {
          this.toastService.success(`Đã ${action} tài khoản thành công!`);
          this.loadMembers();
        },
        error: (err) => {
          console.error(err);
          this.toastService.error(this.extractErrorMessage(err, `Lỗi khi ${action} tài khoản!`));
        }
      });
    }
  }

  // --- Borrowing History ---

  openHistoryModal(member: Member): void {
    this.selectedMemberName.set(member.name);
    this.isHistoryModalOpen.set(true);
    this.isHistoryLoading.set(true);

    this.borrowingService.getBorrowingsByMemberId(member.id).subscribe({
      next: (borrowings: BorrowingResponse[]) => {
        this.memberBorrowings.set(borrowings);
        this.isHistoryLoading.set(false);
      },
      error: (err: any) => {
        console.error('Lỗi khi tải lịch sử:', err);
        this.memberBorrowings.set([]);
        this.isHistoryLoading.set(false);
      }
    });
  }

  closeHistoryModal(): void {
    this.isHistoryModalOpen.set(false);
  }

  isOverdue(borrowing: BorrowingResponse): boolean {
    if (borrowing.returnDate) return false;
    const dueDate = new Date(borrowing.dueDate);
    const now = new Date();
    return now > dueDate;
  }
}
