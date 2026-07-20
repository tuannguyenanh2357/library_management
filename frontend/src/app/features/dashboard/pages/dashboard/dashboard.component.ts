import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { forkJoin } from 'rxjs';
import { BookService } from '@features/books/services/book.service';
import { AuthService } from '@features/auth/services/auth.service';
import { BorrowingRequestService } from '@core/services/borrowing-request.service';
import { ReservationService } from '@features/books/services/reservation.service';
import { BooksResponse } from '@features/books/models/books.model';
import { SearchPanelComponent } from '@shared/components/search-panel/search-panel.component';
import { BookDetailModalComponent } from '@shared/components/book-detail-modal/book-detail-modal.component';
import { ToastService } from '@shared/services/toast.service';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    SearchPanelComponent,
    BookDetailModalComponent
  ],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.css'
})
export class DashboardComponent implements OnInit {
  private bookService = inject(BookService);
  protected authService = inject(AuthService);
  private requestService = inject(BorrowingRequestService);
  private reservationService = inject(ReservationService);
  private toastService = inject(ToastService);

  // States
  protected books = signal<BooksResponse[]>([]);
  protected popularBooks = signal<BooksResponse[]>([]);
  protected loading = signal<boolean>(true);
  protected errorMessage = signal<string>('');

  // Top 10 borrowed books (loaded from real backend database stats)
  protected top10Books = computed(() => {
    return this.popularBooks();
  });

  // Grid of 12 books (3 rows x 4 columns)
  protected featuredBooks = computed(() => {
    return this.books().slice(0, 12);
  });

  // Quản lý trạng thái xem chi tiết sách
  protected isDetailModalOpen = signal<boolean>(false);
  protected selectedBook = signal<BooksResponse | null>(null);

  protected openDetailModal(book: BooksResponse): void {
    this.selectedBook.set(book);
    this.isDetailModalOpen.set(true);
  }

  protected closeDetailModal(): void {
    this.selectedBook.set(null);
    this.isDetailModalOpen.set(false);
  }

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.loading.set(true);
    this.errorMessage.set('');

    // lấy top 10 sách bán chạy nhất tuần
    forkJoin({
      all: this.bookService.getAllBooks(undefined, undefined, undefined, 0, 12),
      popular: this.bookService.getPopularBooks()
    }).subscribe({
      next: (res) => {
        this.books.set(res.all.content);
        this.popularBooks.set(res.popular);
        this.loading.set(false);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage.set('Không thể kết nối với Backend');
        this.loading.set(false);
      }
    });
  }

  protected onBorrowBook(event: { bookId: number, expectedDueDate: string, notes: string }): void {
    if (!this.authService.isLoggedIn()) {
      this.toastService.warning('Vui lòng đăng nhập tài khoản để đăng ký mượn sách!');
      return;
    }

    const memberId = this.authService.getCurrentUserId();
    
    if (!memberId) {
      this.toastService.warning('Tài khoản của bạn cần được cập nhật phiên bản. Vui lòng Đăng xuất và Đăng nhập lại!');
      return;
    }

    const payload = {
      memberId: memberId,
      bookId: event.bookId,
      expectedDueDate: event.expectedDueDate,
      notes: event.notes
    };

    this.requestService.createRequest(payload).subscribe({
      next: () => {
        this.toastService.success('Đăng ký mượn sách thành công! Vui lòng chờ mail phản hồi của chúng tôi.');
        this.closeDetailModal();
      },
      error: (err) => {
        this.toastService.error('Lỗi: ' + (err.error?.message || 'Không thể gửi yêu cầu'));
      }
    });
  }

  protected onReserveBook(event: { bookId: number }): void {
    if (!this.authService.isLoggedIn()) {
      this.toastService.warning('Vui lòng đăng nhập tài khoản để đặt chỗ!');
      return;
    }
    
    const memberId = this.authService.getCurrentUserId();
    
    if (!memberId) {
      this.toastService.warning('Tài khoản của bạn cần được cập nhật phiên bản. Vui lòng Đăng xuất và Đăng nhập lại!');
      return;
    }

    const payload = {
      memberId: memberId,
      bookId: event.bookId
    };

    this.reservationService.createReservation(payload).subscribe({
      next: () => {
        this.toastService.success('Đặt chỗ thành công! Bạn sẽ được ưu tiên nhận sách khi có người trả.');
        this.closeDetailModal();
      },
      error: (err) => {
        this.toastService.error('Lỗi: ' + (err.error?.message || 'Không thể đặt chỗ.'));
      }
    });
  }
}
