import { Component, Input, Output, EventEmitter, inject } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BooksResponse } from '../../models/book.model';
import { CurrentUserService } from '@core/services/current-user.service';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-book-detail-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './book-detail-modal.component.html',
  styleUrl: './book-detail-modal.component.css'
})
export class BookDetailModalComponent {
  private currentUserService = inject(CurrentUserService);
  private authService = inject(AuthService);

  @Input() isOpen = false;
  @Input() book: BooksResponse | null = null;
  
  @Output() close = new EventEmitter<void>();
  @Output() borrow = new EventEmitter<{ bookId: number, expectedDueDate: string, notes: string }>();
  @Output() reserve = new EventEmitter<{ bookId: number }>();

  isBorrowingMode = false;
  expectedDueDate = '';
  notes = '';

  onClose(): void {
    this.isBorrowingMode = false;
    this.expectedDueDate = '';
    this.notes = '';
    this.close.emit();
  }

  enterBorrowMode(): void {
    this.isBorrowingMode = true;
    
    // Default to 14 days from now
    const defaultDate = new Date();
    defaultDate.setDate(defaultDate.getDate() + 14);
    this.expectedDueDate = defaultDate.toISOString().split('T')[0];
  }

  cancelBorrowMode(): void {
    this.isBorrowingMode = false;
  }

  submitBorrow(): void {
    if (this.book && this.expectedDueDate) {
      this.borrow.emit({
        bookId: this.book.id,
        expectedDueDate: this.expectedDueDate,
        notes: this.notes
      });
      this.onClose();
    }
  }

  submitReserve(): void {
    if (this.book) {
      this.reserve.emit({ bookId: this.book.id });
      this.onClose();
    }
  }

  get isLoggedIn(): boolean {
    return this.authService.isLoggedIn();
  }

  isFavorite(): boolean {
    if (!this.book || !this.isLoggedIn) return false;
    return this.currentUserService.favoriteBooks().some(b => b.id === this.book!.id);
  }

  toggleFavorite(): void {
    if (!this.book || !this.isLoggedIn) return;
    
    if (this.isFavorite()) {
      this.currentUserService.removeFavoriteBook(this.book.id).subscribe();
    } else {
      this.currentUserService.addFavoriteBook(this.book.id).subscribe();
    }
  }
}
