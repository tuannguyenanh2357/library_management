import { Routes } from '@angular/router';
import { authGuard } from '@core/guards/auth.guard';
import { adminGuard } from '@core/guards/admin.guard';
import { MainLayoutComponent } from '@layouts/main-layout/main-layout.component';

export const routes: Routes = [
  { 
    path: 'login', 
    loadComponent: () => import('@features/auth/pages/login/login.component').then(m => m.LoginComponent) 
  },
  { 
    path: 'register', 
    loadComponent: () => import('@features/auth/pages/register/register.component').then(m => m.RegisterComponent) 
  },
  { 
    path: '', 
    component: MainLayoutComponent,
    children: [
      { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
      { 
        path: 'dashboard', 
        loadComponent: () => import('@features/dashboard/pages/dashboard/dashboard.component').then(m => m.DashboardComponent) 
      },
      { 
        path: 'books', 
        loadComponent: () => import('@features/books/pages/book-list/book-list.component').then(m => m.BookListComponent) 
      },
      { 
        path: 'admin/dashboard', 
        loadComponent: () => import('@features/admin/pages/admin-dashboard/admin-dashboard.component').then(m => m.AdminDashboardComponent), 
        canActivate: [adminGuard] 
      },
      { 
        path: 'admin/books', 
        loadComponent: () => import('@features/books/pages/admin-book-list/admin-book-list.component').then(m => m.AdminBookListComponent), 
        canActivate: [adminGuard] 
      },
      {
        path: 'admin/members',
        loadComponent: () => import('@features/members/pages/admin-member-list/admin-member-list.component').then(m => m.AdminMemberListComponent),
        canActivate: [adminGuard]
      },
      {
        path: 'admin/borrowings',
        loadComponent: () => import('@features/borrowings/pages/admin-borrowing/admin-borrowing.component').then(m => m.AdminBorrowingComponent),
        canActivate: [adminGuard]
      },
      {
        path: 'admin/fines',
        loadComponent: () => import('@features/fines/pages/admin-fine-list/admin-fine-list.component').then(m => m.AdminFineListComponent),
        canActivate: [adminGuard]
      },
      { 
        path: 'about', 
        loadComponent: () => import('@features/about/pages/about/about.component').then(m => m.AboutComponent) 
      },
      { 
        path: 'profile', 
        loadComponent: () => import('@features/profile/pages/profile/profile.component').then(m => m.ProfileComponent), 
        canActivate: [authGuard] 
      }
    ]
  }
];
