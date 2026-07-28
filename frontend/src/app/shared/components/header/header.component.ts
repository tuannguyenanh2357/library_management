import { Component, inject, signal, HostListener, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink, Router, RouterLinkActive } from '@angular/router';
import { AuthService } from '../../../features/auth/services/auth.service';
import { MemberService } from '../../../features/members/services/member.service';

@Component({
  selector: 'app-header',
  standalone: true,
  imports: [CommonModule, RouterLink, RouterLinkActive],
  templateUrl: './header.component.html',
  styleUrl: './header.component.css'
})
export class HeaderComponent implements OnInit {
  protected authService = inject(AuthService);
  protected memberService = inject(MemberService);
  private router = inject(Router);

  protected isDropdownOpen = signal<boolean>(false);
  protected avatarLoadError = signal<boolean>(false);

  ngOnInit(): void {
    if (this.authService.isLoggedIn()) {
      this.loadUserProfile();
    }
  }

  loadUserProfile(): void {
    if (!this.memberService.currentUserProfile()) {
      this.memberService.getMyProfile().subscribe({
        next: () => this.avatarLoadError.set(false),
        error: (err) => console.error('Failed to fetch profile for header:', err)
      });
    }
  }

  onAvatarError(): void {
    this.avatarLoadError.set(true);
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
    const profile = this.memberService.currentUserProfile();
    if (profile?.name) {
      return profile.name.substring(0, 1).toUpperCase();
    }
    const name = this.authService.getUsername();
    return name ? name.substring(0, 1).toUpperCase() : 'U';
  }

  onLogout(): void {
    this.memberService.clearUserProfile();
    this.authService.logout();
    this.router.navigate(['/dashboard']).then(() => {
      if (typeof window !== 'undefined') {
        window.location.reload();
      }
    });
  }
}
