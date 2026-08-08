import { Component, inject, signal, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, RouterLinkActive } from '@angular/router';
import { AuthService } from '@core/services/auth.service';
import { CurrentUserService } from '@core/services/current-user.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  protected authService = inject(AuthService);
  protected currentUserService = inject(CurrentUserService);
  private router = inject(Router);

  protected isDropdownOpen = signal<boolean>(false);

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadUserProfile();
    }
  }

  loadUserProfile(): void {
    if (!this.currentUserService.currentUserProfile()) {
      this.currentUserService.getMyProfile().subscribe({
        next: () => {},
        error: (err) => console.error('Failed to fetch profile for header:', err)
      });
    }
  }

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.isDropdownOpen.update(v => !v);
  }

  @HostListener('document:click')
  closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }

  getUsernameInitial(): string {
    const profile = this.currentUserService.currentUserProfile();
    if (profile?.name) {
      return profile.name.substring(0, 1).toUpperCase();
    }
    const name = this.authService.getUsername();
    return name ? name.substring(0, 1).toUpperCase() : 'U';
  }

  onLogout(): void {
    this.currentUserService.clearUserProfile();
    this.authService.logout();
    this.router.navigate(['/dashboard']);
  }
}
