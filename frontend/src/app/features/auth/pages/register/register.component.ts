import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.css'
})
export class RegisterComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  protected username = signal<string>('');
  protected password = signal<string>('');
  protected email = signal<string>('');
  protected name = signal<string>('');
  protected phone = signal<string>('');
  protected address = signal<string>('');

  protected errorMessage = signal<string>('');
  protected successMessage = signal<string>('');
  protected loading = signal<boolean>(false);

  onSubmit(): void {
    const phoneStr = this.phone().trim();
    if (
      !this.username().trim() ||
      !this.password().trim() ||
      !this.email().trim() ||
      !this.name().trim() ||
      !this.phone().trim() ||
      !this.address().trim()
    ) {
      this.errorMessage.set('Vui lòng nhập đầy đủ tất cả các trường!');
      return;
    }

    const phoneRegex = /^(\+84|0)[3-9]\d{8}$/;
    if (!phoneRegex.test(phoneStr)) {
      this.errorMessage.set('Số điện thoại không hợp lệ!');
      return;
    }

    this.loading.set(true);
    this.errorMessage.set('');
    this.successMessage.set('');

    this.authService.register({
      username: this.username().trim(),
      password: this.password(),
      email: this.email().trim(),
      name: this.name().trim(),
      phone: phoneStr,
      address: this.address().trim()
    }).subscribe({
      next: () => {
        this.loading.set(false);
        this.successMessage.set('Đăng ký tài khoản thành công! Đang chuyển hướng đăng nhập...');
        setTimeout(() => {
          this.router.navigate(['/login']);
        }, 1000);
      },
      error: (err) => {
        console.error(err);
        this.loading.set(false);
        if (err.status === 400 && err.error && err.error.message) {
          this.errorMessage.set(err.error.message);
        } else {
          this.errorMessage.set('Đăng ký thất bại. Tên đăng nhập hoặc Email hoặc SĐT có thể đã tồn tại!');
        }
      }
    });
  }
}
