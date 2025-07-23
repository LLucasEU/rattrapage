import { Component, OnInit } from '@angular/core';
import { AuthService, UserInfo } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-admin-dashboard',
  templateUrl: './admin-dashboard.component.html',
  styleUrls: ['./admin-dashboard.component.scss'],
})
export class AdminDashboardComponent implements OnInit {
  user: UserInfo | null = null;

  constructor(
    private authService: AuthService,
    private navigationService: NavigationService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    this.user = this.authService.getUser();

    if (!this.authService.isLoggedIn()) {
      this.navigationService.navigateToLogin();
      return;
    }

    if (!this.authService.isAdmin()) {
      this.navigationService.navigateAfterLogin();
      return;
    }
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  goToUserManagement(): void {
    this.router.navigate(['/user-management']);
  }

  goToMovieSearch(): void {
    this.router.navigate(['/movies']);
  }

  goToSystemConfig(): void {
    this.router.navigate(['/system-config']);
  }

  getUserAvatar(): string {
    if (this.user?.username) {
      return this.user.username.charAt(0).toUpperCase();
    }
    return 'A';
  }
}
