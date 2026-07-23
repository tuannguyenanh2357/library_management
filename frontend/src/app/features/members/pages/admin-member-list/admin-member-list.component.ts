import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MemberService, Member, MemberCreationRequest, MemberUpdateRequest, UnpaidMemberProjection } from '../../services/member.service';

import { BorrowingService } from '../../../borrowings/services/borrowing.service';
import { BorrowingResponse } from '../../../../core/models/borrowing.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { ConfirmService } from '../../../../shared/services/confirm.service';
import { AuthService } from '../../../auth/services/auth.service';

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
        this.members.set(data);
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
      password: '', // Leave blank unless they want to change
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

  saveMember(): void {
    const data = this.currentMember();

    // Basic validation
    if (!data.name || !data.email || !data.username) {
      this.toastService.warning('Vui lòng điền các trường bắt buộc (Tên, Email, Username).');
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
          this.toastService.error('Lỗi cập nhật độc giả!');
        }
      });
    } else {
      if (!data.password) {
        this.toastService.warning('Vui lòng nhập mật khẩu cho tài khoản mới.');
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
          this.toastService.error('Lỗi thêm độc giả! Có thể username/email đã tồn tại.');
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
          } else if (err.error && typeof err.error === 'string') {
            this.toastService.error(err.error);
          } else if (err.error && err.error.message) {
            this.toastService.error(err.error.message);
          } else {
            this.toastService.error('Không thể xóa độc giả này. Đã xảy ra lỗi.');
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
          this.toastService.error(`Lỗi khi ${action} tài khoản!`);
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
