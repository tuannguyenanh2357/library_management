import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { AuthService } from '../services/auth.service';

export const adminGuard: CanActivateFn = (route, state) => {
  const authService = inject(AuthService);
  const router = inject(Router);

  // Check if logged in first
  if (!authService.isLoggedIn()) {
    router.navigate(['/login']);
    return false;
  }

  // Check if user has admin/librarian privileges
  if (authService.isAdmin()) {
    return true;
  }

  // User is logged in but doesn't have permissions (403 Forbidden)
  router.navigate(['/forbidden']);
  return false;
};
