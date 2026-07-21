import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { BorrowingService, OverdueBookProjection } from '../../services/borrowing.service';
import { BookCopyService } from '../../../../core/services/book-copy.service';
import { BorrowingRequestService } from '../../../../core/services/borrowing-request.service';
import { BorrowingResponse, BorrowingCreationRequest } from '../../../../core/models/borrowing.model';
import { BookCopyResponse } from '../../../../core/models/book-copy.model';
import { BorrowingRequestResponse, BorrowingRequestApprovalRequest } from '../../../../core/models/borrowing-request.model';
import { ReservationService } from '../../../books/services/reservation.service';
import { ReservationResponse } from '../../../books/models/reservation.model';
import { RouterLink } from '@angular/router';
import { ToastService } from '../../../../shared/services/toast.service';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-admin-borrowing',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, FormsModule, PaginationComponent, RouterLink],
  templateUrl: './admin-borrowing.component.html',
  styleUrl: './admin-borrowing.component.css'
})
export class AdminBorrowingComponent implements OnInit {
  private borrowingService = inject(BorrowingService);
  private bookCopyService = inject(BookCopyService);
  private requestService = inject(BorrowingRequestService);
  private reservationService = inject(ReservationService);
  private fb = inject(FormBuilder);
  private toastService = inject(ToastService);

  Math = Math;

  borrowings = signal<BorrowingResponse[]>([]);
  pendingRequests = signal<BorrowingRequestResponse[]>([]);
  historyRequests = signal<BorrowingRequestResponse[]>([]);
  reservations = signal<ReservationResponse[]>([]);

  overdueBooksSP = signal<OverdueBookProjection[]>([]);
  isOverdueLoading = signal<boolean>(false);

  loadOverdueBooksSP() {
    this.isOverdueLoading.set(true);
    this.borrowingService.getOverdueBooksFromSP().subscribe({
      next: (data) => {
        this.overdueBooksSP.set(data);
        this.isOverdueLoading.set(false);
      },
      error: (err) => {
        console.error('Lỗi khi tải danh sách sách quá hạn từ SP:', err);
        this.toastService.error('Không thể tải danh sách sách quá hạn');
        this.isOverdueLoading.set(false);
      }
    });
  }

  // Search terms
  requestFilters = signal({
    memberName: '',
    bookTitle: '',
    requestDate: '',
    processedDate: '',
    status: '',
    notes: ''
  });

  borrowingFilters = signal({
    memberName: '',
    bookTitle: '',
    barCode: '',
    borrowDate: '',
    returnDate: '',
    status: ''
  });

  filteredHistoryRequests = computed(() => {
    const filters = this.requestFilters();
    return this.historyRequests().filter(req => {
      const matchMember = !filters.memberName || req.memberName?.toLowerCase().includes(filters.memberName.toLowerCase());
      const matchBook = !filters.bookTitle || req.bookTitle?.toLowerCase().includes(filters.bookTitle.toLowerCase());
      
      // Translate status for searching
      const translatedStatus = req.status === 'APPROVED' ? 'đã duyệt' : (req.status === 'REJECTED' ? 'từ chối' : req.status?.toLowerCase() || '');
      const matchStatus = !filters.status || translatedStatus.includes(filters.status.toLowerCase());
      
      const matchNotes = !filters.notes || req.notes?.toLowerCase().includes(filters.notes.toLowerCase());
      
      const reqDateStr = req.requestDate ? new Date(req.requestDate).toLocaleDateString('en-US') : ''; // Matches 'shortDate' pipe format roughly or we can just use substring
      const matchReqDate = !filters.requestDate || req.requestDate?.includes(filters.requestDate);
      
      const matchProcDate = !filters.processedDate || req.processedDate?.includes(filters.processedDate);

      return matchMember && matchBook && matchStatus && matchNotes && matchReqDate && matchProcDate;
    });
  });

  filteredBorrowings = computed(() => {
    const filters = this.borrowingFilters();
    return this.borrowings().filter(b => {
      const matchMember = !filters.memberName || b.memberName?.toLowerCase().includes(filters.memberName.toLowerCase());
      const matchBook = !filters.bookTitle || b.bookTitle?.toLowerCase().includes(filters.bookTitle.toLowerCase());
      const matchBarCode = !filters.barCode || b.barCode?.toLowerCase().includes(filters.barCode.toLowerCase());
      
      const statusStr = b.returnDate ? 'đã trả' : 'đang mượn';
      const matchStatus = !filters.status || statusStr.includes(filters.status.toLowerCase());
      
      const matchBorrowDate = !filters.borrowDate || b.borrowDate?.includes(filters.borrowDate);
      const matchReturnDate = !filters.returnDate || b.returnDate?.includes(filters.returnDate);

      return matchMember && matchBook && matchBarCode && matchBorrowDate && matchReturnDate && matchStatus;
    });
  });

  // Pagination for requests
  reqCurrentPage = signal(1);
  reqPageSize = signal(10);
  paginatedHistoryRequests = computed(() => {
    const start = (this.reqCurrentPage() - 1) * this.reqPageSize();
    const end = start + this.reqPageSize();
    return this.filteredHistoryRequests().slice(start, end);
  });
  reqPagesArray = computed(() => {
    const totalPages = Math.ceil(this.filteredHistoryRequests().length / this.reqPageSize());
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  });

  // Pagination for borrowings
  borrowCurrentPage = signal(1);
  borrowPageSize = signal(10);
  paginatedBorrowings = computed(() => {
    const start = (this.borrowCurrentPage() - 1) * this.borrowPageSize();
    const end = start + this.borrowPageSize();
    return this.filteredBorrowings().slice(start, end);
  });
  borrowPagesArray = computed(() => {
    const totalPages = Math.ceil(this.filteredBorrowings().length / this.borrowPageSize());
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  });

  updateRequestFilter(field: keyof typeof this.requestFilters.prototype, value: string) {
    this.requestFilters.update(f => ({ ...f, [field]: value }));
    this.reqCurrentPage.set(1); // Reset to page 1 on filter
  }

  updateBorrowingFilter(field: keyof typeof this.borrowingFilters.prototype, value: string) {
    this.borrowingFilters.update(f => ({ ...f, [field]: value }));
    this.borrowCurrentPage.set(1); // Reset to page 1 on filter
  }

  goToReqPage(page: number) {
    if (page >= 1 && page <= this.reqPagesArray().length) {
      this.reqCurrentPage.set(page);
    }
  }

  goToBorrowPage(page: number) {
    if (page >= 1 && page <= this.borrowPagesArray().length) {
      this.borrowCurrentPage.set(page);
    }
  }

  scannedBookCopy = signal<BookCopyResponse | null>(null);
  scanError = signal<string>('');
  scanWarning = signal<string>('');

  borrowForm: FormGroup;
  returnForm: FormGroup;

  loading = signal<boolean>(false);
  activeTab = signal<'APPROVAL' | 'BORROW' | 'RETURN' | 'HISTORY' | 'RESERVATIONS' | 'OVERDUE_SP'>('APPROVAL');

  // State for approval modal
  selectedRequestToApprove = signal<BorrowingRequestResponse | null>(null);
  isRejecting = signal<boolean>(false);
  rejectReason = signal<string>('');

  constructor() {
    this.borrowForm = this.fb.group({
      memberId: ['', Validators.required],
      barcode: ['', Validators.required],
      dueDate: ['', Validators.required]
    });

    this.returnForm = this.fb.group({
      barcode: ['', Validators.required]
    });
  }

  ngOnInit() {
    this.loadBorrowings();
    this.loadPendingRequests();
    this.loadHistoryRequests();
    this.loadReservations();
    this.loadOverdueBooksSP();

    const defaultDueDate = new Date();
    defaultDueDate.setDate(defaultDueDate.getDate() + 14);
    const dueDateStr = defaultDueDate.toISOString().split('T')[0];

    this.borrowForm.patchValue({ dueDate: dueDateStr });
  }

  loadBorrowings() {
    this.borrowingService.getAllBorrowings().subscribe({
      next: (data) => this.borrowings.set(data),
      error: (err) => console.error('Lỗi khi tải danh sách mượn sách', err)
    });
  }

  loadPendingRequests() {
    this.loading.set(true);
    this.requestService.getPendingRequests().subscribe({
      next: (data) => {
        this.pendingRequests.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Lỗi tải danh sách yêu cầu chờ duyệt', err);
        this.loading.set(false);
      }
    });
  }

  loadReservations() {
    this.reservationService.getAllReservations().subscribe({
      next: (data) => this.reservations.set(data),
      error: (err) => console.error('Lỗi khi tải danh sách hàng đợi', err)
    });
  }

  loadHistoryRequests() {
    this.requestService.getHistoryRequests().subscribe({
      next: (data) => this.historyRequests.set(data),
      error: (err) => console.error('Lỗi khi tải lịch sử duyệt yêu cầu', err)
    });
  }

  scanBarcodeForBorrow() {
    const barcode = this.borrowForm.value.barcode;
    if (!barcode) return;

    this.scanError.set('');
    this.scanWarning.set('');
    this.scannedBookCopy.set(null);

    this.bookCopyService.getBookCopyByBarcode(barcode).subscribe({
      next: (copy) => {
        this.scannedBookCopy.set(copy);
        if (copy.status !== 'AVAILABLE' && copy.status !== 'RESERVED') {
          this.scanError.set('Sách này hiện không có sẵn.');
        } else if (copy.status === 'RESERVED') {
          this.scanWarning.set('Bạn đã tạo lượt mượn thành công');
        }
      },
      error: () => this.scanError.set('Không tìm thấy mã vạch này.')
    });
  }

  onSubmitBorrow() {
    if (this.borrowForm.invalid || !this.scannedBookCopy() || (this.scannedBookCopy()?.status !== 'AVAILABLE' && this.scannedBookCopy()?.status !== 'RESERVED')) return;

    const request: BorrowingCreationRequest = {
      memberId: this.borrowForm.value.memberId,
      bookCopyId: this.scannedBookCopy()!.id,
      dueDate: this.borrowForm.value.dueDate
    };

    this.borrowingService.borrowBook(request).subscribe({
      next: () => {
        this.toastService.success('Cho mượn sách thành công!');
        this.borrowForm.reset();
        this.scannedBookCopy.set(null);
        this.loadBorrowings();
        this.ngOnInit();
      },
      error: (err) => this.toastService.error('Lỗi: ' + (err.error?.message || 'Lỗi hệ thống'))
    });
  }

  onSubmitReturn() {
    if (this.returnForm.invalid) return;

    const barcode = this.returnForm.value.barcode;
    const activeBorrowing = this.borrowings().find(b => b.barCode === barcode && !b.returnDate);

    if (!activeBorrowing) {
      this.toastService.warning('Không tìm thấy phiếu mượn nào chưa trả cho mã vạch này.');
      return;
    }

    this.borrowingService.returnBook(activeBorrowing.id).subscribe({
      next: () => {
        this.toastService.success('Trả sách thành công!');
        this.returnForm.reset();
        this.loadBorrowings();
      },
      error: (err) => this.toastService.error('Lỗi khi trả sách: ' + err.error?.message)
    });
  }

  openApprovalModal(req: BorrowingRequestResponse) {
    this.selectedRequestToApprove.set(req);
    this.isRejecting.set(false);
    this.rejectReason.set('');
  }

  closeApprovalModal() {
    this.selectedRequestToApprove.set(null);
    this.isRejecting.set(false);
    this.rejectReason.set('');
  }

  submitApproval() {
    const req = this.selectedRequestToApprove();
    if (!req) return;

    this.requestService.approveRequest(req.id).subscribe({
      next: () => {
        this.toastService.success('Đã duyệt và tự động giao sách thành công!');
        this.closeApprovalModal();
        this.loadPendingRequests();
        this.loadBorrowings();
        this.loadHistoryRequests();
      },
      error: (err) => this.toastService.error('Lỗi khi duyệt: ' + err.error?.message)
    });
  }

  toggleRejectMode() {
    this.isRejecting.set(true);
  }

  cancelRejectMode() {
    this.isRejecting.set(false);
    this.rejectReason.set('');
  }

  updateRejectReason(event: any) {
    this.rejectReason.set(event.target.value);
  }

  submitReject() {
    const req = this.selectedRequestToApprove();
    if (!req || !this.rejectReason().trim()) {
      this.toastService.warning('Vui lòng nhập lý do từ chối!');
      return;
    }

    this.requestService.rejectRequest(req.id, this.rejectReason()).subscribe({
      next: () => {
        this.toastService.success('Đã từ chối yêu cầu mượn sách!');
        this.closeApprovalModal();
        this.loadPendingRequests();
        this.loadHistoryRequests();
      },
      error: (err) => this.toastService.error('Lỗi: ' + err.error?.message)
    });
  }
}
