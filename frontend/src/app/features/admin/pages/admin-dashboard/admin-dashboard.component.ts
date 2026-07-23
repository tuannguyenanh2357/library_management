import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../services/admin.service';
import { BorrowingService } from '../../../borrowings/services/borrowing.service';
import { DashboardStatsResponse } from '../../models/admin.model';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterLink],
  templateUrl: './admin-dashboard.component.html',
  styleUrl: './admin-dashboard.component.css'
})
export class AdminDashboardComponent implements OnInit {
  private adminService = inject(AdminService);
  private borrowingService = inject(BorrowingService);

  protected stats = signal<DashboardStatsResponse | null>(null);
  protected loading = signal<boolean>(true);
  protected error = signal<string>('');

  protected currentDate = signal<string>('');
  protected greeting = signal<string>('');

  protected activities = signal<any[]>([]);

  protected systemHealth = signal({
    dbStatus: 'CONNECTED',
    serverLoad: 'Normal (12%)',
    activeSessions: 3,
    syncTime: 'Vừa xong'
  });

  ngOnInit(): void {
    this.loadStats();
    this.loadActivities();
    this.updateGreetingAndDate();
  }

  loadActivities(): void {
    this.borrowingService.getAllBorrowings().subscribe({
      next: (data) => {
        // Lấy 4 giao dịch mới nhất
        const recent = data.sort((a, b) => b.id - a.id).slice(0, 8);
        const formattedActivities = recent.map((b, index) => {
          const isReturned = b.returnDate != null;
          return {
            id: index + 1,
            type: isReturned ? 'return' : 'borrow',
            user: 'Thành viên: ' + (b.memberName || 'thành viên').replace(/\s+/g, '').toLowerCase(),
            action: isReturned ? 'vừa trả sách' : 'vừa mượn sách',
            detail: b.bookTitle,
            time: isReturned ? (b.returnDate || 'Gần đây') : (b.borrowDate || 'Gần đây'),
            icon: isReturned ? '✅' : '📖',
            class: isReturned ? 'green' : 'blue'
          };
        });
        this.activities.set(formattedActivities);
      },
      error: (err) => {
        console.error('Error fetching activities', err);
      }
    });
  }

  loadStats(): void {
    this.loading.set(true);
    this.adminService.getDashboardStats().subscribe({
      next: (data) => {
        this.stats.set(data);
        this.loading.set(false);
      },
      error: (err) => {
        console.error('Error fetching dashboard stats', err);
        this.error.set('Lỗi khi tải dữ liệu thống kê');
        this.loading.set(false);
      }
    });
  }

  private updateGreetingAndDate(): void {
    const now = new Date();

    // Greeting
    const hour = now.getHours();
    if (hour < 12) {
      this.greeting.set('Chào buổi sáng ☀️');
    } else if (hour < 18) {
      this.greeting.set('Chào buổi chiều 🌤️');
    } else {
      this.greeting.set('Chào buổi tối 🌙');
    }

    // Format date
    const days = ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'];
    const dayName = days[now.getDay()];
    const dateStr = now.toLocaleDateString('vi-VN', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric'
    });
    this.currentDate.set(`${dayName}, ${dateStr}`);
  }
}
