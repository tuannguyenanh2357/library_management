import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { FineService } from '../../services/fine.service';
import { FineResponse } from '../../models/fine.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { ConfirmService } from '../../../../shared/services/confirm.service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-admin-fine-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, PaginationComponent],
  templateUrl: './admin-fine-list.component.html',
  styleUrls: ['./admin-fine-list.component.css']
})
export class AdminFineListComponent implements OnInit {
  private fineService = inject(FineService);
  private toastService = inject(ToastService);
  private confirmService = inject(ConfirmService);

  fines = signal<FineResponse[]>([]);
  isLoading = signal<boolean>(true);
  errorMessage = signal<string>('');

  // Tab: 'UNPAID' | 'PAID' | 'CANCELLED'
  activeTab = signal<'UNPAID' | 'PAID' | 'CANCELLED'>('UNPAID');

  // Filters
  filterMemberName = signal<string>('');
  filterDateFrom = signal<string>('');
  filterDateTo = signal<string>('');

  // Stats
  totalUnpaid = computed(() => this.fines().filter(f => f.status === 'UNPAID').length);
  totalPaid = computed(() => this.fines().filter(f => f.status === 'PAID').length);
  totalCancelled = computed(() => this.fines().filter(f => f.status === 'CANCELLED').length);
  totalUnpaidAmount = computed(() =>
    this.fines()
      .filter(f => f.status === 'UNPAID')
      .reduce((sum, f) => sum + Number(f.amount), 0)
  );
  totalPaidAmount = computed(() =>
    this.fines()
      .filter(f => f.status === 'PAID')
      .reduce((sum, f) => sum + Number(f.amount), 0)
  );

  filteredFines = computed(() => {
    let result = this.fines().filter(f => f.status === this.activeTab());

    const nameSearch = this.filterMemberName().toLowerCase().trim();
    if (nameSearch) {
      result = result.filter(f => f.memberName.toLowerCase().includes(nameSearch));
    }

    const from = this.filterDateFrom();
    const to = this.filterDateTo();
    if (from) {
      result = result.filter(f => f.issuedDate && f.issuedDate >= from);
    }
    if (to) {
      result = result.filter(f => f.issuedDate && f.issuedDate <= to);
    }

    return result;
  });

  // Pagination
  currentPage = signal<number>(1);
  pageSize = 10;

  paginatedFines = computed(() => {
    const start = (this.currentPage() - 1) * this.pageSize;
    return this.filteredFines().slice(start, start + this.pageSize);
  });

  goToPage(page: number): void {
    const totalPages = Math.ceil(this.filteredFines().length / this.pageSize) || 1;
    if (page >= 1 && page <= totalPages) {
      this.currentPage.set(page);
    }
  }

  switchTab(tab: 'UNPAID' | 'PAID' | 'CANCELLED'): void {
    this.activeTab.set(tab);
    this.filterMemberName.set('');
    this.filterDateFrom.set('');
    this.filterDateTo.set('');
    this.currentPage.set(1);
  }

  onFilterChange(name?: string, dateFrom?: string, dateTo?: string): void {
    if (name !== undefined) this.filterMemberName.set(name);
    if (dateFrom !== undefined) this.filterDateFrom.set(dateFrom);
    if (dateTo !== undefined) this.filterDateTo.set(dateTo);
    this.currentPage.set(1);
  }

  clearFilters(): void {
    this.filterMemberName.set('');
    this.filterDateFrom.set('');
    this.filterDateTo.set('');
    this.currentPage.set(1);
  }

  hasActiveFilters = computed(() =>
    !!this.filterMemberName() || !!this.filterDateFrom() || !!this.filterDateTo()
  );

  ngOnInit(): void {
    this.loadFines();
  }

  loadFines(): void {
    this.isLoading.set(true);
    this.errorMessage.set('');

    this.fineService.getAllFines().subscribe({
      next: (data) => {
        this.fines.set(data);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Lỗi khi tải danh sách phạt:', err);
        this.errorMessage.set('Không thể kết nối với Backend');
        this.isLoading.set(false);
      }
    });
  }

  async payFine(fine: FineResponse): Promise<void> {
    const isConfirmed = await this.confirmService.confirm(
      `Xác nhận thu ${Number(fine.amount).toLocaleString('vi-VN')} VND phạt từ độc giả "${fine.memberName}" (sách: ${fine.bookTitle})?`
    );
    if (isConfirmed) {
      this.fineService.payFine(fine.id).subscribe({
        next: (updatedFine) => {
          // Cập nhật trực tiếp trong danh sách mà không cần reload để giữ lịch sử
          this.fines.update(list =>
            list.map(f => f.id === fine.id ? updatedFine : f)
          );
          this.toastService.success(`✅ Đã thu ${Number(fine.amount).toLocaleString('vi-VN')} VND từ ${fine.memberName}`);
          // Chuyển sang tab lịch sử để thấy bản ghi vừa thu
          this.switchTab('PAID');
        },
        error: (err) => {
          console.error('Lỗi khi thanh toán khoản phạt:', err);
          this.toastService.error('Thanh toán thất bại! Vui lòng thử lại.');
        }
      });
    }
  }

  async cancelFine(fine: FineResponse): Promise<void> {
    const reason = prompt(`Nhập lý do miễn khoản phạt của "${fine.memberName}" (sách: ${fine.bookTitle}):`);
    if (reason === null) return;

    const isConfirmed = await this.confirmService.confirm(
      `Xác nhận miễn khoản phạt ${Number(fine.amount).toLocaleString('vi-VN')} VND của độc giả "${fine.memberName}"?`
    );
    if (isConfirmed) {
      this.fineService.cancelFine(fine.id, reason).subscribe({
        next: (updatedFine) => {
          this.fines.update(list =>
            list.map(f => f.id === fine.id ? updatedFine : f)
          );
          this.toastService.success(`✅ Đã miễn khoản phạt cho ${fine.memberName}`);
          this.switchTab('CANCELLED');
        },
        error: (err) => {
          console.error('Lỗi khi miễn khoản phạt:', err);
          this.toastService.error('Miễn phạt thất bại! Vui lòng thử lại.');
        }
      });
    }
  }
}
