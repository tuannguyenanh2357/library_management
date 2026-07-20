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

  // Filters
  filterStatus = signal<string>('ALL'); // 'ALL', 'UNPAID', 'PAID'
  filterMemberName = signal<string>('');

  filteredFines = computed(() => {
    let result = this.fines();
    
    // Status filter
    if (this.filterStatus() !== 'ALL') {
      result = result.filter(f => f.status === this.filterStatus());
    }

    // Member name filter
    const nameSearch = this.filterMemberName().toLowerCase().trim();
    if (nameSearch) {
      result = result.filter(f => f.memberName.toLowerCase().includes(nameSearch));
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

  // Reset page when filters change
  onFilterChange(status?: string, name?: string) {
    if (status !== undefined) this.filterStatus.set(status);
    if (name !== undefined) this.filterMemberName.set(name);
    this.currentPage.set(1);
  }

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
    const isConfirmed = await this.confirmService.confirm(`Thanh toán khoản phạt ${fine.amount.toLocaleString()} VND cho độc giả ${fine.memberName}?`);
    if (isConfirmed) {
      this.fineService.payFine(fine.id).subscribe({
        next: () => {
          this.toastService.success('Thanh toán thành công!');
          this.loadFines(); // Tải lại danh sách
        },
        error: (err) => {
          console.error('Lỗi khi thanh toán khoản phạt:', err);
          this.toastService.error('Thanh toán thất bại!');
        }
      });
    }
  }
}
