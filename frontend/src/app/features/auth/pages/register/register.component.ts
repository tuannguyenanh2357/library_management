import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '@core/services/auth.service';

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

  protected showPassword = signal<boolean>(false);
  protected errorMessage = signal<string>('');

  toggleShowPassword(): void {
    this.showPassword.update(v => !v);
  }
  protected successMessage = signal<string>('');
  protected loading = signal<boolean>(false);

  onSubmit(): void {
    const phoneStr = this.phone().trim();
    if (!this.username().trim()) {
      this.errorMessage.set('Tên đăng nhập không được để trống');
      return;
    }
    if (!this.password().trim()) {
      this.errorMessage.set('Mật khẩu không được để trống');
      return;
    }
    if (!this.email().trim()) {
      this.errorMessage.set('Email không được để trống');
      return;
    }
    if (!this.name().trim()) {
      this.errorMessage.set('Họ tên không được để trống');
      return;
    }
    if (!this.phone().trim()) {
      this.errorMessage.set('Số điện thoại không được để trống');
      return;
    }
    if (!this.address().trim()) {
      this.errorMessage.set('Địa chỉ không được để trống');
      return;
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/;
    if (!emailRegex.test(this.email().trim())) {
      this.errorMessage.set('Email không đúng định dạng');
      return;
    }

    const phoneRegex = /(\+84|0)[3-9]\d{8}$/;
    if (!phoneRegex.test(phoneStr)) {
      this.errorMessage.set('Số điện thoại không đúng định dạng');
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
