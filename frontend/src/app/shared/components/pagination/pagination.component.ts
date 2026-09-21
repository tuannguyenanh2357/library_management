import { Component, Input, Output, EventEmitter, computed, input, signal } from '@angular/core';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-pagination',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pagination-container">
      <div class="pagination-info">
        @if (totalItems() === 0) {
          Không có dữ liệu
        } @else {
          Hiển thị từ {{ startItem() }} đến {{ endItem() }} trong tổng số {{ totalItems() }} {{ itemLabel() }}
        }
      </div>
      <div class="pagination-controls">
        <button
          class="btn-page"
          [disabled]="currentPage() <= 1"
          (click)="onPageChange(currentPage() - 1)"
        >
          Trang trước
        </button>

        <div class="page-numbers">
          @for (page of visiblePages(); track page) {
            @if (page === -1) {
              <span class="page-ellipsis">…</span>
            } @else {
              <button
                class="btn-page"
                [class.active]="page === currentPage()"
                (click)="onPageChange(page)"
              >
                {{ page }}
              </button>
            }
          }
        </div>

        <button
          class="btn-page"
          [disabled]="currentPage() >= totalPages()"
          (click)="onPageChange(currentPage() + 1)"
        >
          Trang sau
        </button>
      </div>
    </div>
  `,
  styles: [`
    .pagination-container {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 15px 0;
      margin-top: 10px;
      flex-wrap: wrap;
      gap: 10px;
    }
    .pagination-info {
      color: #64748b;
      font-size: 0.9rem;
    }
    .pagination-controls {
      display: flex;
      align-items: center;
      gap: 5px;
    }
    .page-numbers {
      display: flex;
      gap: 5px;
    }
    .btn-page {
      padding: 6px 14px;
      border: 1px solid #e2e8f0;
      background: white;
      border-radius: 6px;
      cursor: pointer;
      color: #475569;
      font-size: 0.875rem;
      font-weight: 500;
      transition: all 0.15s ease;
    }
    .btn-page:hover:not(:disabled) {
      border-color: #4f46e5;
      color: #4f46e5;
      background: #f5f3ff;
    }
    .btn-page.active {
      background: #4f46e5;
      color: white;
      border-color: #4f46e5;
    }
    .btn-page:disabled {
      opacity: 0.4;
      cursor: not-allowed;
    }
    .page-ellipsis {
      padding: 6px 4px;
      color: #94a3b8;
      font-size: 0.875rem;
    }
  `]
})
export class PaginationComponent {
  /** Trang hiện tại (1-indexed) */
  currentPage = input.required<number>();

  /** Tổng số items */
  totalItems = input.required<number>();

  /** Số items mỗi trang */
  pageSize = input.required<number>();

  /** Nhãn loại item, ví dụ: "đầu sách", "bản sao", "độc giả" */
  itemLabel = input<string>('mục');

  /** Emit trang mới khi người dùng click */
  @Output() pageChange = new EventEmitter<number>();

  totalPages = computed(() => Math.ceil(this.totalItems() / this.pageSize()) || 1);

  startItem = computed(() => {
    if (this.totalItems() === 0) return 0;
    return (this.currentPage() - 1) * this.pageSize() + 1;
  });

  endItem = computed(() =>
    Math.min(this.currentPage() * this.pageSize(), this.totalItems())
  );

  /** Tạo danh sách trang hiển thị với dấu "..." khi nhiều trang */
  visiblePages = computed(() => {
    const total = this.totalPages();
    const current = this.currentPage();
    if (total <= 7) {
      return Array.from({ length: total }, (_, i) => i + 1);
    }
    const pages: number[] = [];
    pages.push(1);
    if (current > 3) pages.push(-1); // ellipsis
    for (let i = Math.max(2, current - 1); i <= Math.min(total - 1, current + 1); i++) {
      pages.push(i);
    }
    if (current < total - 2) pages.push(-1); // ellipsis
    pages.push(total);
    return pages;
  });

  onPageChange(page: number): void {
    if (page >= 1 && page <= this.totalPages() && page !== this.currentPage()) {
      this.pageChange.emit(page);
    }
  }
}
