import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../services/auth.service';

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
        this.errorMessage.set('Tên đăng nhập hoặc mật khẩu không đúng!');
      }
    });
  }
}
