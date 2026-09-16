import { ApplicationConfig, provideBrowserGlobalErrorListeners, provideAppInitializer, inject } from '@angular/core';
import { provideRouter, withPreloading, PreloadAllModules } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';
import { firstValueFrom } from 'rxjs';

import { routes } from './app.routes';
import { authInterceptor } from './core/interceptors/auth.interceptor';
import { AuthService } from './core/services/auth.service';

// 1. Tách riêng hàm khôi phục User khi vừa load web
function initializeApp() {
  const authService = inject(AuthService);
  // Gọi /auth/me để lấy user, nếu lỗi thì ngầm cho qua (trả về Promise)
  return firstValueFrom(authService.fetchCurrentUser());
}

// 2. Cấu hình appConfig ngắn gọn, dễ nhìn
export const appConfig: ApplicationConfig = {
  providers: [
    provideBrowserGlobalErrorListeners(),
    provideRouter(routes, withPreloading(PreloadAllModules)),
    provideHttpClient(withFetch(), withInterceptors([authInterceptor])),

    // Tự động kiểm tra Cookie & khôi phục User trước khi hiển thị UI
    provideAppInitializer(initializeApp)
  ]
};
