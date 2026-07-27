import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MemberService, Member, ProfileUpdateRequest, ChangePasswordRequest } from '../../../members/services/member.service';
import { BorrowingService } from '@features/borrowings/services/borrowing.service';
import { AuthService } from '@features/auth/services/auth.service';
import { BorrowingRequestService } from '@core/services/borrowing-request.service';
import { BorrowingRequestResponse } from '@core/models/borrowing-request.model';
import { BorrowingResponse } from '@core/models/borrowing.model';
import { ReservationService } from '../../../books/services/reservation.service';
import { ReservationResponse } from '../../../books/models/reservation.model';
import { FileService } from '@core/services/file.service';
import { FineService } from '@features/fines/services/fine.service';
import { FineResponse } from '@features/fines/models/fine.model';


@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.css'
})
export class ProfileComponent implements OnInit {
  private memberService = inject(MemberService);
  private borrowingService = inject(BorrowingService);
  private borrowingRequestService = inject(BorrowingRequestService);
  private reservationService = inject(ReservationService);
  private fileService = inject(FileService);
  private fineService = inject(FineService);
  private authService = inject(AuthService);
  private router = inject(Router);

  // Profile data state
  protected profile = signal<Member | null>(null);
  protected loading = signal<boolean>(true);
  protected errorMessage = signal<string>('');

  // Borrowing history states
  protected borrowings = signal<BorrowingResponse[]>([]);
  protected onlineRequests = signal<BorrowingRequestResponse[]>([]);
  protected myReservations = signal<ReservationResponse[]>([]);
  protected myFines = signal<FineResponse[]>([]);
  protected activeTab = signal<string>('history'); // 'history' | 'edit' | 'password' | 'reservations' | 'fines'

  // Form edit states
  protected editForm = signal<ProfileUpdateRequest>({
    name: '',
    email: '',
    phone: '',
    address: '',
    avatar: ''
  });
  protected updating = signal<boolean>(false);
  protected updateSuccess = signal<boolean>(false);
  protected updateError = signal<string>('');

  // Password form states
  protected passwordForm = signal({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  protected changingPassword = signal<boolean>(false);
  protected passwordSuccess = signal<boolean>(false);
  protected passwordError = signal<string>('');

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.router.navigate(['/login']);
      return;
    }
    this.loadProfileAndHistory();
  }

  loadProfileAndHistory(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    this.memberService.getMyProfile().subscribe({
      next: (member) => {
        this.profile.set(member);
        // Initialize form fields
        this.editForm.set({
          name: member.name,
          email: member.email,
          phone: member.phone,
          address: member.address,
          avatar: member.avatar
        });

        // Fetch borrowings
        this.borrowingService.getBorrowingsByMemberId(member.id).subscribe({
          next: (borrowList) => {
            this.borrowings.set(borrowList);
            this.checkLoadingComplete();
          },
          error: (err) => {
            console.error('Lỗi khi tải lịch sử mượn:', err);
            this.checkLoadingComplete();
          }
        });

        // Fetch online requests
        this.borrowingRequestService.getRequestsByMember(member.id).subscribe({
          next: (requestList) => {
            this.onlineRequests.set(requestList);
            this.checkLoadingComplete();
          },
          error: (err) => {
            console.error('Lỗi khi tải lịch sử yêu cầu online:', err);
            this.checkLoadingComplete();
          }
        });

        // Fetch reservations
        this.reservationService.getMyReservations().subscribe({
          next: (reservations) => {
            this.myReservations.set(reservations);
            this.checkLoadingComplete();
          },
          error: (err) => {
            console.error('Lỗi khi tải danh sách đặt chỗ:', err);
            this.checkLoadingComplete();
          }
        });

        // Fetch my fines
        this.fineService.getFinesByMemberId(member.id).subscribe({
          next: (fines) => {
            this.myFines.set(fines);
            this.checkLoadingComplete();
          },
          error: (err) => {
            console.error('Lỗi khi tải danh sách khoản phạt:', err);
            this.checkLoadingComplete();
          }
        });
      },
      error: (err) => {
        console.error('Lỗi khi tải thông tin cá nhân:', err);
        const detailedMsg = err?.error || err?.message || 'Không rõ nguyên nhân';
        this.errorMessage.set(`Không thể tải thông tin cá nhân. Chi tiết: ${detailedMsg}`);
        this.loading.set(false);
      }
    });
  }

  private loadCount = 0;
  private checkLoadingComplete() {
    this.loadCount++;
    if (this.loadCount >= 4) {
      this.loading.set(false);
      this.loadCount = 0;
    }
  }

  protected totalBorrowingsCount = () => this.borrowings().length;

  protected currentlyBorrowedCount = () => {
    return this.borrowings().filter(b => !b.returnDate && !this.isOverdue(b)).length;
  };

  protected returnedCount = () => {
    return this.borrowings().filter(b => !!b.returnDate).length;
  };

  protected overdueCount = () => {
    return this.borrowings().filter(b => this.isOverdue(b)).length;
  };

  protected isOverdue(b: BorrowingResponse): boolean {
    if (b.returnDate) return false;
    if (!b.dueDate) return false;
    const due = new Date(b.dueDate);
    const now = new Date();
    due.setHours(0, 0, 0, 0);
    now.setHours(0, 0, 0, 0);
    return due < now;
  }

  protected getBorrowingStatus(b: BorrowingResponse): string {
    if (b.returnDate) return 'returned';
    if (this.isOverdue(b)) return 'overdue';
    return 'borrowing';
  }

  protected getStatusText(b: BorrowingResponse): string {
    if (b.returnDate) return 'Đã trả';
    if (this.isOverdue(b)) return 'Quá hạn';
    return 'Đang mượn';
  }

  protected getRequestStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'borrowing'; // using existing blue badge
      case 'APPROVED': return 'returned';  // using existing green badge
      case 'REJECTED': return 'overdue';   // using existing red badge
      default: return '';
    }
  }

  protected getRequestStatusText(status: string): string {
    switch (status) {
      case 'PENDING': return 'Đang Chờ Duyệt';
      case 'APPROVED': return 'Đã Duyệt';
      case 'REJECTED': return 'Từ Chối';
      default: return status;
    }
  }

  protected getReservationStatusClass(status: string): string {
    switch (status) {
      case 'PENDING': return 'borrowing';
      case 'FULFILLED': return 'returned';
      case 'COMPLETED': return 'returned';
      case 'CANCELLED': return 'overdue';
      case 'EXPIRED': return 'overdue';
      default: return '';
    }
  }

  protected getReservationStatusText(status: string): string {
    switch (status) {
      case 'PENDING': return 'Đang xếp hàng';
      case 'FULFILLED': return 'Sách đã về (Đang giữ)';
      case 'COMPLETED': return 'Đã đến lấy';
      case 'CANCELLED': return 'Đã hủy';
      case 'EXPIRED': return 'Hết hạn giữ';
      default: return status;
    }
  }

  protected canRenew(b: BorrowingResponse): boolean {
    return !b.returnDate && !this.isOverdue(b) && (b.renewalCount ?? 0) < 1;
  }

  protected renewBorrowing(b: BorrowingResponse): void {
    this.borrowingService.renewBorrowing(b.id).subscribe({
      next: () => {
        this.loadProfileAndHistory();
      },
      error: (err) => {
        console.error('Lỗi khi gia hạn mượn sách:', err);
        alert('Không thể gia hạn lúc này. Lỗi: ' + (err?.error?.message || 'Không xác định'));
      }
    });
  }

  protected cancelOnlineRequest(id: number): void {
    if (confirm('Bạn có chắc chắn muốn hủy yêu cầu mượn sách này?')) {
      this.borrowingRequestService.cancelRequest(id).subscribe({
        next: () => {
          this.loadProfileAndHistory();
        },
        error: (err) => {
          console.error('Lỗi khi hủy yêu cầu mượn:', err);
          alert('Không thể hủy yêu cầu lúc này. Lỗi: ' + (err?.error?.message || 'Không xác định'));
        }
      });
    }
  }

  protected getFineStatusClass(status: string): string {
    switch (status) {
      case 'UNPAID': return 'overdue';
      case 'PAID': return 'returned';
      case 'CANCELLED': return '';
      default: return '';
    }
  }

  protected getFineStatusText(status: string): string {
    switch (status) {
      case 'UNPAID': return 'Chưa thanh toán';
      case 'PAID': return 'Đã thanh toán';
      case 'CANCELLED': return 'Đã miễn';
      default: return status;
    }
  }

  protected cancelReservation(id: number): void {
    if (confirm('Bạn có chắc chắn muốn hủy đặt chỗ này? Nếu có sách đang giữ, sách sẽ được nhường cho người tiếp theo.')) {
      this.reservationService.cancelReservation(id).subscribe({
        next: () => {
          this.loadProfileAndHistory(); // Reload để cập nhật
        },
        error: (err) => {
          console.error('Lỗi khi hủy đặt chỗ:', err);
          alert('Không thể hủy đặt chỗ lúc này. Lỗi: ' + (err?.error?.message || 'Không xác định'));
        }
      });
    }
  }

  protected onUpdateProfile(): void {
    const data = this.editForm();
    if (!data.name || !data.email) {
      this.updateError.set('Họ tên và Email là bắt buộc!');
      return;
    }

    this.updating.set(true);
    this.updateSuccess.set(false);
    this.updateError.set('');

    const request: ProfileUpdateRequest = {
      name: data.name,
      email: data.email,
      phone: data.phone,
      address: data.address,
      avatar: data.avatar
    };

    this.memberService.updateMyProfile(request).subscribe({
      next: (updatedMember) => {
        this.profile.set(updatedMember);
        this.updating.set(false);
        this.updateSuccess.set(true);
      },
      error: (err) => {
        console.error('Lỗi khi cập nhật profile:', err);
        this.updateError.set(err?.error?.message || 'Cập nhật thông tin thất bại. Vui lòng thử lại.');
        this.updating.set(false);
      }
    });
  }

  protected onFileSelected(event: any): void {
    const file = event.target.files[0];
    if (file) {
      this.fileService.uploadFile(file).subscribe({
        next: (res) => {
          this.editForm.update(f => ({ ...f, avatar: res.url }));
        },
        error: (err) => {
          console.error(err);
          this.updateError.set('Tải ảnh đại diện lên thất bại!');
        }
      });
    }
  }

  protected onChangePassword(): void {
    const data = this.passwordForm();
    if (!data.oldPassword || !data.newPassword || !data.confirmPassword) {
      this.passwordError.set('Vui lòng nhập đầy đủ thông tin!');
      return;
    }
    if (data.newPassword !== data.confirmPassword) {
      this.passwordError.set('Mật khẩu mới không khớp!');
      return;
    }

    this.changingPassword.set(true);
    this.passwordSuccess.set(false);
    this.passwordError.set('');

    this.memberService.changePassword({
      oldPassword: data.oldPassword,
      newPassword: data.newPassword
    }).subscribe({
      next: () => {
        this.changingPassword.set(false);
        this.passwordSuccess.set(true);
        this.passwordForm.set({ oldPassword: '', newPassword: '', confirmPassword: '' });
      },
      error: (err) => {
        this.passwordError.set(err?.error?.message || 'Đổi mật khẩu thất bại.');
        this.changingPassword.set(false);
      }
    });
  }

  protected logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
