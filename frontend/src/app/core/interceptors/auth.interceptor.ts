import { HttpInterceptorFn } from '@angular/common/http';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  // Sao chép yêu cầu để luôn bao gồm thông tin xác thực (cookie)
  const cloned = req.clone({
    withCredentials: true
  });

  return next(cloned);
};
