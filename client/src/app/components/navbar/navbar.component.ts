import { Component, OnInit } from '@angular/core';
import { AuthService, UserInfo } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
})
export class NavbarComponent implements OnInit {
  isLoggedIn = false;
  user: UserInfo | null = null;
  isAdmin = false;

  constructor(
    private authService: AuthService,
    private navigationService: NavigationService,
  ) {}

  ngOnInit(): void {
    this.checkAuthStatus();
  }

  checkAuthStatus(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.user = this.authService.getUser();
    this.isAdmin = this.authService.isAdmin();
  }

  logout(): void {
    this.authService.logout();
    this.checkAuthStatus();
    this.navigationService.navigateToHome();
  }

  goToDashboard(): void {
    this.navigationService.navigateAfterLogin();
  }

  getUserInitial(): string {
    if (this.user?.username) {
      return this.user.username.charAt(0).toUpperCase();
    }
    return 'U';
  }

  getUserName(): string {
    return this.user?.username || 'Utilisateur';
  }
}
