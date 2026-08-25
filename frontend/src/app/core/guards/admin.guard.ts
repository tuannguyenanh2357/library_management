import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Kiểm tra xem đã đăng nhập chưa
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // Kiểm tra xem người dùng có quyền admin
  if (authService.isAdmin()) {
    return true;
  }

  // Người dùng đã đăng nhập nhưng không có quyền (403 Forbidden)
  router.navigate(['/forbidden']);
  return false;
};
