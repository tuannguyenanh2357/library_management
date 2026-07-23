import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../../features/auth/services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if logged in and has admin/librarian privileges
  if (authService.isLoggedIn() && authService.isAdmin()) {
    return true;
  }

  // Redirect to login if unauthorized
  router.navigate(['/login']);
  return false;
};
