import { HttpInterceptorFn, HttpErrorResponse } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const authService = inject(AuthService);

  // Sao chép yêu cầu để luôn bao gồm thông tin xác thực (cookie)
  const cloned = req.clone({
    withCredentials: true // đính kèm cookie đăng nhập vào mọi request tới BE
  });

  return next(cloned).pipe(
    catchError((error: HttpErrorResponse) => {
      // Chỉ đăng xuất nếu nhận lỗi 401 Unauthorized (Session hết hạn / chưa đăng nhập)
      // Lỗi 403 Forbidden (không có quyền thực hiện thao tác) sẽ giữ nguyên phiên đăng nhập
      if (error.status === 401) {
        authService.clearUserDetails();
        // Không redirect nếu lỗi 401 đến từ endpoint khôi phục session /auth/me
        if (!req.url.endsWith('/auth/me')) {
          router.navigate(['/login']);
        }
      }
      return throwError(() => error);
    })
  );
};
