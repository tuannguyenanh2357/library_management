import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { BookService } from '../../services/book.service';
import { BooksResponse, CreateBookRequest } from '../../models/books.model';
import { FileService } from '../../../../core/services/file.service';
import { BookCopyService } from '../../../../core/services/book-copy.service';
import { BookCopyResponse } from '../../../../core/models/book-copy.model';
import { BorrowingService } from '../../../borrowings/services/borrowing.service';
import { BorrowingResponse } from '../../../../core/models/borrowing.model';
import { ToastService } from '../../../../shared/services/toast.service';
import { ConfirmService } from '../../../../shared/services/confirm.service';
import { Subject, forkJoin } from 'rxjs';
import { debounceTime } from 'rxjs/operators';

import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { PaginationComponent } from '../../../../shared/components/pagination/pagination.component';

@Component({
  selector: 'app-admin-book-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink, PaginationComponent],
  templateUrl: './admin-book-list.component.html',
  styleUrl: './admin-book-list.component.css'
})
export class AdminBookListComponent implements OnInit {
  private bookService = inject(BookService);
  private bookCopyService = inject(BookCopyService);
  private borrowingService = inject(BorrowingService);
  private fileService = inject(FileService);
  private toastService = inject(ToastService);
  private confirmService = inject(ConfirmService);

  // Tabs
  protected activeTab = signal<'books' | 'copies'>('books');
  protected Math = Math;

  // States
  protected books = signal<BooksResponse[]>([]);
  protected loading = signal<boolean>(true);
  protected errorMessage = signal<string>('');

  // Book Copies States
  protected copies = signal<BookCopyResponse[]>([]);
  protected loadingCopies = signal<boolean>(false);
  protected allBooksList = signal<BooksResponse[]>([]);
  protected isAddCopyModalOpen = signal<boolean>(false);
  protected selectedBookIdForCopy = signal<number | null>(null);
  protected numberOfCopiesToAdd = signal<number>(1);

  // Edit/History Book Copy States
  protected isEditCopyModalOpen = signal<boolean>(false);
  protected selectedCopy = signal<BookCopyResponse | null>(null);
  protected editCopyStatus = signal<string>('AVAILABLE');

  protected isHistoryModalOpen = signal<boolean>(false);
  protected copyHistory = signal<BorrowingResponse[]>([]);
  protected loadingHistory = signal<boolean>(false);

  // Pagination states for Books
  protected currentPage = signal<number>(0);
  protected totalPages = signal<number>(0);
  protected totalElements = signal<number>(0);
  protected pageSize = 8;

  // Pagination states for Copies
  protected copiesCurrentPage = signal<number>(1);
  protected copiesPageSize = signal<number>(10);

  // Search Signals - Books
  protected searchId = signal<number | null>(null);
  protected searchTitle = signal<string>('');
  protected searchAuthor = signal<string>('');
  protected searchCategory = signal<string>('');
  protected searchPublisher = signal<string>('');
  protected searchIsbn = signal<string>('');

  // Search Signals - Book Copies
  protected searchCopyId = signal<string>('');
  protected searchCopyBarcode = signal<string>('');
  protected searchCopyTitle = signal<string>('');
  protected searchCopyAuthor = signal<string>('');
  protected searchCopyStatus = signal<string>('');
  protected searchCopyDueDate = signal<string>('');

  // Computed filtered list for book copies
  protected filteredCopies = computed(() => {
    const list = this.copies();
    const id = this.searchCopyId().toLowerCase().trim();
    const barcode = this.searchCopyBarcode().toLowerCase().trim();
    const title = this.searchCopyTitle().toLowerCase().trim();
    const author = this.searchCopyAuthor().toLowerCase().trim();
    const status = this.searchCopyStatus().toLowerCase().trim();
    const dueDate = this.searchCopyDueDate().toLowerCase().trim();

    return list.filter(copy => {
      const matchId = !id || copy.id.toString().includes(id);
      const matchBarcode = !barcode || copy.barCode.toLowerCase().includes(barcode);
      const matchTitle = !title || (copy.title && copy.title.toLowerCase().includes(title));
      const matchAuthor = !author || (copy.author && copy.author.toLowerCase().includes(author));
      const matchStatus = !status || copy.status.toLowerCase() === status;
      const matchDueDate = !dueDate || (copy.dueDate && copy.dueDate.includes(dueDate));
      return matchId && matchBarcode && matchTitle && matchAuthor && matchStatus && matchDueDate;
    });
  });

  // Computed paginated copies
  protected paginatedCopies = computed(() => {
    const start = (this.copiesCurrentPage() - 1) * this.copiesPageSize();
    const end = start + this.copiesPageSize();
    return this.filteredCopies().slice(start, end);
  });

  protected copiesPagesArray = computed(() => {
    const totalPages = Math.ceil(this.filteredCopies().length / this.copiesPageSize());
    return Array.from({ length: totalPages }, (_, i) => i + 1);
  });

  protected goToCopiesPage(page: number) {
    if (page >= 1 && page <= this.copiesPagesArray().length) {
      this.copiesCurrentPage.set(page);
    }
  }

  private searchSubject = new Subject<void>();

  ngOnInit(): void {
    this.loadBooks(0);
    this.searchSubject.pipe(debounceTime(300)).subscribe(() => {
      this.loadBooks(0);
    });
  }

  loadBooks(pageNumber: number = 0): void {
    this.loading.set(true);
    this.errorMessage.set('');
    this.currentPage.set(pageNumber);

    this.bookService.getAllBooks(
      this.searchTitle() || undefined,
      this.searchAuthor() || undefined,
      this.searchCategory() || undefined,
      pageNumber,
      this.pageSize,
      this.searchId() || undefined,
      this.searchPublisher() || undefined,
      this.searchIsbn() || undefined
    ).subscribe({
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
  }

  protected onSearchBooks(): void {
    this.searchSubject.next();
  }

  protected onResetBooks(): void {
    this.searchId.set(null);
    this.searchTitle.set('');
    this.searchAuthor.set('');
    this.searchCategory.set('');
    this.searchPublisher.set('');
    this.searchIsbn.set('');
    this.loadBooks(0);
  }

  protected onResetCopies(): void {
    this.searchCopyId.set('');
    this.searchCopyBarcode.set('');
    this.searchCopyTitle.set('');
    this.searchCopyAuthor.set('');
    this.searchCopyStatus.set('');
    this.searchCopyDueDate.set('');
    this.copiesCurrentPage.set(1);
  }

  protected onPageChange(pageNumber: number): void {
    if (pageNumber < 0 || pageNumber >= this.totalPages()) return;
    this.loadBooks(pageNumber);
  }

  /** Wrapper for PaginationComponent (1-indexed) -> loadBooks (0-indexed) */
  protected onBooksPageChange(page: number): void {
    this.onPageChange(page - 1);
  }

  protected pagesList = computed(() => {
    return Array.from({ length: this.totalPages() }, (_, i) => i);
  });

  async onDelete(id: number): Promise<void> {
    if (await this.confirmService.confirm('Bạn có chắc chắn muốn xóa cuốn sách này không?')) {
      this.bookService.deleteBook(id).subscribe({
        next: () => {
          this.loadBooks(this.currentPage());
          this.toastService.success('Xóa sách thành công!');
        },
        error: (err) => {
          console.error(err);
          this.toastService.error('Xóa sách thất bại! Cuốn sách này có thể đang có độc giả mượn.');
        }
      });
    }
  }

  // Quản lý trạng thái Sửa Sách
  protected isEditModalOpen = signal<boolean>(false);
  protected selectedBook = signal<BooksResponse | null>(null);

  protected openEditModal(book: BooksResponse) {
    this.selectedBook.set({ ...book });
    this.isEditModalOpen.set(true);
  }

  closeEditModal(): void {
    this.isEditModalOpen.set(false);
    this.selectedBook.set(null);
  }

  onUpdateBook(): void {
    const booktoUpdate = this.selectedBook();
    if (!booktoUpdate) return;

    const updateRequest = {
      id: booktoUpdate.id,
      title: booktoUpdate.title,
      author: booktoUpdate.author,
      isbn: booktoUpdate.isbn,
      publisher: booktoUpdate.publisher,
      category: booktoUpdate.category,
      description: booktoUpdate.description,
      publicationYear: booktoUpdate.publicationYear,
      imageUrl: booktoUpdate.imageUrl,
      dailyFineAmount: booktoUpdate.dailyFineAmount
    }

    this.bookService.updateBook(booktoUpdate.id, updateRequest).subscribe({
      next: (updateBook) => {
        this.loadBooks(this.currentPage());
        this.toastService.success('Cập nhật sách thành công!');
        this.closeEditModal();
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Cập nhật sách thất bại!');
      }
    });
  }

  // Quản lý trạng thái Thêm Sách Mới
  protected isAddModalOpen = signal<boolean>(false);
  protected newBook = signal<CreateBookRequest>({
    title: '',
    author: '',
    isbn: '',
    publisher: '',
    category: '',
    description: '',
    publicationYear: new Date().getFullYear(),
    imageUrl: '',
    dailyFineAmount: 5000
  });

  protected openAddModal(): void {
    this.newBook.set({
      title: '',
      author: '',
      isbn: '',
      publisher: '',
      category: '',
      description: '',
      publicationYear: new Date().getFullYear(),
      imageUrl: '',
      dailyFineAmount: 5000
    });
    this.isAddModalOpen.set(true);
  }

  closeAddModal(): void {
    this.isAddModalOpen.set(false);
  }

  onCreateBook(): void {
    const bookToCreate = this.newBook();
    if (!bookToCreate.title || !bookToCreate.author || !bookToCreate.isbn || !bookToCreate.category) {
      this.toastService.warning('Vui lòng nhập đầy đủ các thông tin bắt buộc (Tiêu đề, Tác giả, ISBN, Thể loại)!');
      return;
    }

    this.bookService.createBook(bookToCreate).subscribe({
      next: (createdBook) => {
        this.loadBooks(0);
        this.toastService.success('Thêm sách mới thành công!');
        this.closeAddModal();
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Thêm sách mới thất bại! Vui lòng kiểm tra lại (mã ISBN có thể đã tồn tại).');
      }
    });
  }

  protected onFileSelected(event: any, type: 'new' | 'edit'): void {
    const file = event.target.files[0];
    if (file) {
      this.fileService.uploadFile(file).subscribe({
        next: (res) => {
          if (type === 'new') {
            this.newBook().imageUrl = res.url;
          } else if (type === 'edit') {
            this.selectedBook()!.imageUrl = res.url;
          }
          this.toastService.success('Tải ảnh lên thành công!');
        },
        error: (err) => {
          console.error(err);
          this.toastService.error('Tải ảnh lên thất bại!');
        }
      });
    }
  }

  // Tab methods
  protected selectTab(tab: 'books' | 'copies'): void {
    this.activeTab.set(tab);
    if (tab === 'copies') {
      this.loadCopies();
    } else {
      this.loadBooks(0);
    }
  }

  protected loadCopies(): void {
    this.loadingCopies.set(true);
    this.errorMessage.set('');
    this.bookCopyService.getAllBookCopies().subscribe({
      next: (res) => {
        this.copies.set(res);
        this.loadingCopies.set(false);
      },
      error: (err) => {
        console.error(err);
        this.errorMessage.set('Không thể kết nối với Backend để lấy danh sách bản sao');
        this.loadingCopies.set(false);
      }
    });
  }

  protected async onDeleteCopy(id: number): Promise<void> {
    if (await this.confirmService.confirm('Bạn có chắc chắn muốn xóa bản sao vật lý này không?')) {
      this.bookCopyService.deleteBookCopy(id).subscribe({
        next: () => {
          this.loadCopies();
          this.toastService.success('Xóa bản sao thành công!');
        },
        error: (err) => {
          console.error(err);
          if (err.error && typeof err.error === 'string') {
            this.toastService.error(err.error);
          } else if (err.error && err.error.message) {
            this.toastService.error(err.error.message);
          } else {
            this.toastService.error('Xóa bản sao thất bại!');
          }
        }
      });
    }
  }

  protected openAddCopyModal(): void {
    this.selectedBookIdForCopy.set(null);
    this.allBooksList.set([]);
    this.bookService.getAllBooks(undefined, undefined, undefined, 0, 1000).subscribe({
      next: (res) => {
        this.allBooksList.set(res.content);
        this.isAddCopyModalOpen.set(true);
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Không thể tải danh sách đầu sách để tạo bản sao.');
      }
    });
  }

  protected closeAddCopyModal(): void {
    this.isAddCopyModalOpen.set(false);
    this.selectedBookIdForCopy.set(null);
    this.numberOfCopiesToAdd.set(1);
    this.customBarcode.set('');
  }

  protected customBarcode = signal<string>('');

  protected onCreateCopy(): void {
    const bookId = this.selectedBookIdForCopy();
    const quantity = this.numberOfCopiesToAdd();

    if (!bookId) {
      this.toastService.warning('Vui lòng chọn một đầu sách!');
      return;
    }

    if (quantity < 1 || quantity > 50) {
      this.toastService.warning('Số lượng bản sao mỗi lần thêm phải từ 1 đến 50!');
      return;
    }

    const requests = Array.from({ length: quantity }, (_, index) => {
      let barcode = this.customBarcode().trim() || undefined;
      if (barcode && quantity > 1) {
        barcode = `${barcode}-${index + 1}`;
      }
      return this.bookCopyService.createBookCopy({ bookId, barCode: barcode });
    });

    forkJoin(requests).subscribe({
      next: () => {
        this.loadCopies();
        this.toastService.success(`Thêm thành công ${quantity} bản sao mới!`);
        this.closeAddCopyModal();
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Trùng mã BARCODE với bản sao khác!');
      }
    });
  }

  // Edit/History methods
  protected openEditCopyModal(copy: BookCopyResponse): void {
    this.selectedCopy.set({ ...copy });
    this.editCopyStatus.set(copy.status);
    this.isEditCopyModalOpen.set(true);
  }

  protected closeEditCopyModal(): void {
    this.isEditCopyModalOpen.set(false);
    this.selectedCopy.set(null);
  }

  protected onUpdateCopyStatus(): void {
    const copy = this.selectedCopy();
    if (!copy || !copy.bookId) {
      this.toastService.warning('Thông tin bản sao không đầy đủ.');
      return;
    }

    if (copy.status === 'BORROWED' || copy.status === 'RESERVED') {
      this.toastService.error('Không thể sửa trạng thái của bản sao đang được mượn hoặc giữ chỗ.');
      return;
    }

    this.bookCopyService.updateBookCopy(copy.id, {
      bookId: copy.bookId,
      status: this.editCopyStatus()
    }).subscribe({
      next: () => {
        this.loadCopies();
        this.toastService.success('Cập nhật trạng thái bản sao thành công!');
        this.closeEditCopyModal();
      },
      error: (err) => {
        console.error(err);
        this.toastService.error('Cập nhật trạng thái bản sao thất bại!');
      }
    });
  }

  protected openHistoryModal(copy: BookCopyResponse): void {
    this.selectedCopy.set(copy);
    this.copyHistory.set([]);
    this.loadingHistory.set(true);
    this.isHistoryModalOpen.set(true);

    this.borrowingService.getBorrowingsByCopyId(copy.id).subscribe({
      next: (res) => {
        this.copyHistory.set(res);
        this.loadingHistory.set(false);
      },
      error: (err) => {
        console.error(err);
        this.loadingHistory.set(false);
        this.toastService.error('Không thể tải lịch sử mượn của bản sao này.');
      }
    });
  }

  protected closeHistoryModal(): void {
    this.isHistoryModalOpen.set(false);
    this.selectedCopy.set(null);
    this.copyHistory.set([]);
  }
}
