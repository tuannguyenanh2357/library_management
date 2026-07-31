import { Component, OnInit, inject, signal, computed, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { BookService } from '@features/books/services/book.service';
import { BooksResponse } from '@shared/models/book.model';
import { AuthService } from '@core/services/auth.service';
import { BorrowingRequestService } from '@features/borrowings/services/borrowing-request.service';
import { ReservationService } from '../../services/reservation.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SearchPanelComponent } from '@shared/components/search-panel/search-panel.component';
import { BookDetailModalComponent } from '@shared/components/book-detail-modal/book-detail-modal.component';
import { ToastService } from '@shared/services/toast.service';

@Component({
  selector: 'app-book-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    SearchPanelComponent,
    BookDetailModalComponent
  ],
  templateUrl: './book-list.component.html',
  styleUrl: './book-list.component.css'
})
export class BookListComponent implements OnInit {
  private bookService = inject(BookService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private destroyRef = inject(DestroyRef);
  protected authService = inject(AuthService);
  private requestService = inject(BorrowingRequestService);
  private reservationService = inject(ReservationService);
  private toastService = inject(ToastService);

  // States
  protected books = signal<BooksResponse[]>([]);
  protected categories = signal<string[]>([]);
  protected loading = signal<boolean>(true);
  protected errorMessage = signal<string>('');

  // Pagination states
  protected currentPage = signal<number>(0);
  protected totalPages = signal<number>(0);
  protected totalElements = signal<number>(0);
  protected pageSize = 12; // 4 books per page

  // Filters
  protected filterTitle = signal<string>('');
  protected filterAuthor = signal<string>('');
  protected filterCategory = signal<string>('');

  // Detail Modal states
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
    this.loadBooksAndFilter();
    this.bookService.getCategories().subscribe({
      next: (data) => this.categories.set(data),
      error: (err) => console.error('Lỗi khi tải danh sách thể loại:', err)
    });
  }

  loadBooksAndFilter(): void {
    // Lắng nghe sự thay đổi của các tham số trên URL để lọc & phân trang động
    this.route.queryParams
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.loading.set(true);
        this.errorMessage.set('');

        const title = (params['title'] || '').trim();
        const author = (params['author'] || '').trim();
        const category = (params['category'] || '').trim();
        const pageParam = params['page'] ? Number(params['page']) : 1;

        // Đồng bộ bộ lọc hiển thị
        this.filterTitle.set(title);
        this.filterAuthor.set(author);
        this.filterCategory.set(category);

        // 0-based page for spring API
        const apiPage = Math.max(0, pageParam - 1);
        this.currentPage.set(apiPage);

        this.bookService.getAllBooks(title, author, category, apiPage, this.pageSize).subscribe({
          next: (res) => {
            this.books.set(res.content);
            this.totalPages.set(res.totalPages);
            this.totalElements.set(res.totalElements);
            this.loading.set(false);
          },
          error: (err) => {
            console.error(err);
            this.errorMessage.set('Không thể kết nối với Backend');
            this.loading.set(false);
          }
        });
      });
  }

  protected onPageChange(pageNumber: number): void {
    if (pageNumber < 0 || pageNumber >= this.totalPages()) return;

    this.router.navigate([], {
      relativeTo: this.route,
      queryParams: { page: pageNumber + 1 },
      queryParamsHandling: 'merge'
    });
  }

  protected pagesList = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    const pages: number[] = [];

    let start = Math.max(0, current - 2);
    let end = Math.min(total - 1, current + 2);

    if (current - 2 < 0) {
      end = Math.min(total - 1, end + (2 - current));
    }
    if (current + 2 >= total) {
      start = Math.max(0, start - (2 - (total - 1 - current)));
    }

    for (let i = start; i <= end; i++) {
      pages.push(i);
    }
    return pages;
  });

  protected onBorrowBook(event: { bookId: number, expectedDueDate: string, notes: string }): void {
    if (!this.authService.isLoggedIn()) {
      this.toastService.warning('Vui lòng đăng nhập tài khoản để đăng ký mượn sách!');
      return;
    }
    
    // MemberId is presumably fetched from token/authService in backend, but our DTO requires it.
    // Wait, the backend requires memberId in the DTO?
    // Let's check authService to get the memberId.
    const memberId = this.authService.getCurrentUserId();
    
    if (!memberId) {
      this.toastService.warning('Không lấy được thông tin tài khoản. Vui lòng đăng nhập lại.');
      return;
    }

    const payload = {
      memberId: memberId,
      bookId: event.bookId,
      expectedDueDate: event.expectedDueDate,
      notes: event.notes
    };

    // Need to inject BorrowingRequestService
    this.requestService.createRequest(payload).subscribe({
      next: () => {
        this.toastService.success('Đăng ký mượn sách thành công! Yêu cầu của bạn đã được gửi tới thủ thư.');
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
      this.toastService.warning('Không lấy được thông tin tài khoản. Vui lòng đăng nhập lại.');
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
