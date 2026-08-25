import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const authGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Kiểm tra xem đã đăng nhập chưa
  if (authService.isLoggedIn()) {
    return true;
  }

  // Chuyển hướng sang trang đăng nhập nếu chưa đăng nhập
  router.navigate(['/login']);
  return false;
};
