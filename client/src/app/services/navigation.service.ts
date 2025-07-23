import { Injectable } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({
  providedIn: 'root',
})
export class NavigationService {
  constructor(
    private router: Router,
    private authService: AuthService,
  ) {}

  navigateAfterLogin(): void {
    if (this.authService.isAdmin()) {
      this.router.navigate(['/admin-dashboard']);
    } else if (this.authService.isUser()) {
      this.router.navigate(['/user-dashboard']);
    } else {
      this.router.navigate(['/user-dashboard']);
    }
  }

  navigateAfterRegister(): void {
    this.navigateAfterLogin();
  }

  navigateToLogin(): void {
    this.router.navigate(['/login']);
  }

  navigateToRegister(): void {
    this.router.navigate(['/register']);
  }

  navigateToHome(): void {
    this.router.navigate(['/home']);
  }
}
