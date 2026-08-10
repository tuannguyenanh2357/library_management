import { Component, Input, OnInit, inject, DestroyRef } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';

@Component({
  selector: 'app-search-panel',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './search-panel.component.html',
  styleUrl: './search-panel.component.css'
})
export class SearchPanelComponent implements OnInit {
  private router = inject(Router);
  private route = inject(ActivatedRoute);
  private destroyRef = inject(DestroyRef);

  @Input() categories: string[] = [];

  protected searchTitle = '';
  protected searchAuthor = '';
  protected selectedCategory = '';

  ngOnInit(): void {
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
