import { Component, OnInit } from '@angular/core';
import { AuthService } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss'],
})
export class HomeComponent implements OnInit {
  isLoggedIn = false;
  isAdmin = false;

  constructor(
    private authService: AuthService,
    private navigationService: NavigationService,
  ) {}

  ngOnInit(): void {
    this.isLoggedIn = this.authService.isLoggedIn();
    this.isAdmin = this.authService.isAdmin();
  }

  goToDashboard(): void {
    this.navigationService.navigateAfterLogin();
  }

  goToLogin(): void {
    this.navigationService.navigateToLogin();
  }

  goToRegister(): void {
    this.navigationService.navigateToRegister();
  }
}
