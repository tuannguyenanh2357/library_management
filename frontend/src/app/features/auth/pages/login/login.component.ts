import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  protected username = signal<string>('');
  protected password = signal<string>('');
  protected errorMessage = signal<string>('');
  protected loading = signal<boolean>(false);

  onSubmit(): void {
    if (!this.username() || !this.password()) {
      this.errorMessage.set('Vui lòng nhập đầy đủ tên đăng nhập và mật khẩu!');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');

    this.authService.login({
      username: this.username().trim(),
      password: this.password()
    }).subscribe({
      next: (response) => {
        this.loading.set(false);
        if (response.authenticated) {
          if (this.authService.isAdmin()) {
            this.router.navigate(['/admin/dashboard']);
          } else {
            this.router.navigate(['/dashboard']);
          }
        } else {
          this.errorMessage.set('Đăng nhập không thành công!');
        }
      },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
        
        if (err.status === 0) {
          this.errorMessage.set('Không thể kết nối đến máy chủ. Máy chủ có thể đang tắt!');
        } else if (err.status === 500) {
          this.errorMessage.set('Máy chủ đang gặp sự cố (Lỗi 500). Vui lòng thử lại sau!');
        } else if (err.status === 401 || err.status === 403 || err.status === 400) {
          this.errorMessage.set('Tên đăng nhập hoặc mật khẩu không đúng!');
        } else {
          this.errorMessage.set(err.error?.message || 'Đã có lỗi xảy ra, vui lòng thử lại!');
        }
      }
    });
  }
}
