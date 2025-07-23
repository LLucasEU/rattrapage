import { Component, OnInit } from '@angular/core';
import { AuthService, UserInfo } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';
import { Router } from '@angular/router';
import { HttpClient, HttpHeaders } from '@angular/common/http';

export interface User {
  id: number;
  username: string;
  email: string;
  role: string;
}

@Component({
  selector: 'app-user-management',
  templateUrl: './user-management.component.html',
  styleUrls: ['./user-management.component.scss'],
})
export class UserManagementComponent implements OnInit {
  users: User[] = [];
  currentUser: UserInfo | null = null;
  loading = false;
  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private navigationService: NavigationService,
    private router: Router,
    private http: HttpClient,
  ) {}

  ngOnInit(): void {
    this.currentUser = this.authService.getUser();

    if (!this.authService.isLoggedIn()) {
      this.navigationService.navigateToLogin();
      return;
    }

    if (!this.authService.isAdmin()) {
      this.navigationService.navigateAfterLogin();
      return;
    }

    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';

    this.http
      .get<User[]>('http://localhost:8080/api/v1/users', {
        headers: this.getAuthHeaders(),
      })
      .subscribe({
        next: (users) => {
          this.users = users;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error loading users:', error);
          this.errorMessage = 'Erreur lors du chargement des utilisateurs';
          this.loading = false;
        },
      });
  }

  updateUserRole(userId: number, newRole: string): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    const updateData = {
      role: newRole,
    };

    this.http
      .put(`http://localhost:8080/api/v1/users/${userId}/role`, updateData, {
        headers: this.getAuthHeaders(),
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Rôle utilisateur mis à jour avec succès !';
          this.loadUsers();
          this.loading = false;

          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error updating user role:', error);
          this.errorMessage = 'Erreur lors de la mise à jour du rôle';
          this.loading = false;
        },
      });
  }

  deleteUser(userId: number): void {
    if (!confirm('Êtes-vous sûr de vouloir supprimer cet utilisateur ?')) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';

    this.http
      .delete(`http://localhost:8080/api/v1/users/${userId}`, {
        headers: this.getAuthHeaders(),
      })
      .subscribe({
        next: () => {
          this.successMessage = 'Utilisateur supprimé avec succès !';
          this.loadUsers();
          this.loading = false;

          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
        },
        error: (error) => {
          console.error('Error deleting user:', error);
          this.errorMessage = "Erreur lors de la suppression de l'utilisateur";
          this.loading = false;
        },
      });
  }

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  getRoleDisplayName(role: string): string {
    return role === 'ADMIN' ? 'Administrateur' : 'Utilisateur';
  }

  canModifyUser(user: User): boolean {
    return this.currentUser?.id !== user.id;
  }

  canDeleteUser(user: User): boolean {
    return this.currentUser?.id !== user.id;
  }

  getAdminCount(): number {
    return this.users.filter((u) => u.role === 'ADMIN').length;
  }

  getUserCount(): number {
    return this.users.filter((u) => u.role === 'USER').length;
  }

  onRoleChange(userId: number, event: Event): void {
    const target = event.target as HTMLSelectElement;
    if (target && target.value) {
      this.updateUserRole(userId, target.value);
    }
  }
}
