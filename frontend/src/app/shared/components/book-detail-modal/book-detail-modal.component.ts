import { Component, Input, Output, EventEmitter } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { BooksResponse } from '../../../features/books/models/books.model';

@Component({
  selector: 'app-book-detail-modal',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './book-detail-modal.component.html',
  styleUrl: './book-detail-modal.component.css'
})
export class BookDetailModalComponent {
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
}
