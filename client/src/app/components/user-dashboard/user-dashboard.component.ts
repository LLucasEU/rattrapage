import { Component, OnInit } from '@angular/core';
import { AuthService, UserInfo } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';
import { Router } from '@angular/router';

@Component({
  selector: 'app-user-dashboard',
  templateUrl: './user-dashboard.component.html',
  styleUrls: ['./user-dashboard.component.scss'],
})
export class UserDashboardComponent implements OnInit {
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

    if (this.authService.isAdmin()) {
      this.navigationService.navigateAfterLogin();
      return;
    }
  }

  goToSettings(): void {
    this.router.navigate(['/settings']);
  }

  goToMovieSearch(): void {
    this.router.navigate(['/movies']);
  }

  gotToStats(): void {
    this.router.navigate(['/stats']);
  }

  getUserAvatar(): string {
    if (this.user?.username) {
      return this.user.username.charAt(0).toUpperCase();
    }
    return 'U';
  }
}
