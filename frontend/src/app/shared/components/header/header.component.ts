import { Component, inject, signal, HostListener } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent {
  protected authService = inject(AuthService);
  private router = inject(Router);

  protected isDropdownOpen = signal<boolean>(false);

  toggleDropdown(event: Event): void {
    event.stopPropagation();
    this.isDropdownOpen.update(v => !v);
  }

  @HostListener('document:click')
  closeDropdown(): void {
    this.isDropdownOpen.set(false);
  }

  getUsernameInitial(): string {
    const name = this.authService.getUsername();
    return name ? name.substring(0, 1).toUpperCase() : 'U';
  }

  onLogout(): void {
    this.authService.logout();
    this.router.navigate(['/dashboard']).then(() => {
      if (typeof window !== 'undefined') {
        window.location.reload();
      }
    });
  }
}
