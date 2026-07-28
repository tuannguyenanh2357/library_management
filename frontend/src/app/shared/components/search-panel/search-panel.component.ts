import { Component, OnInit, inject, signal, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { BookService } from '../../../features/books/services/book.service';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-search-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-panel.component.html',
  styleUrl: './search-panel.component.css'
})
export class SearchPanelComponent implements OnInit {
  private bookService = inject(BookService);
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  protected searchTitle = '';
  protected searchAuthor = '';
  protected selectedCategory = '';
  protected categories = signal<string[]>([]);

  ngOnInit(): void {
    this.bookService.getCategories().subscribe({
      next: (data) => {
        this.categories.set(data);
      },
      error: (err) => console.error('Lỗi khi tải danh sách thể loại:', err)
    });

    this.route.queryParams
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(params => {
        this.searchTitle = params['title'] || '';
        this.searchAuthor = params['author'] || '';
        this.selectedCategory = params['category'] || '';
      });
  }

  onSearch(): void {
    this.router.navigate(['/books'], {
      queryParams: {
        title: this.searchTitle.trim() || null,
        author: this.searchAuthor.trim() || null,
        category: this.selectedCategory || null
      }
    });
  }
}
