import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import {
  AuthService,
  UserInfo,
  UpdateProfileRequest,
  ChangePasswordRequest,
} from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.scss'],
})
export class SettingsComponent implements OnInit {
  user: UserInfo | null = null;
  profileForm: FormGroup;
  passwordForm: FormGroup;
  loading = false;
  profileSuccessMessage = '';
  passwordSuccessMessage = '';
  profileErrorMessage = '';
  passwordErrorMessage = '';

  constructor(
    private formBuilder: FormBuilder,
    private authService: AuthService,
    private navigationService: NavigationService,
  ) {
    this.profileForm = this.formBuilder.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      email: ['', [Validators.required, Validators.email]],
    });

    this.passwordForm = this.formBuilder.group(
      {
        currentPassword: ['', [Validators.required, Validators.minLength(6)]],
        newPassword: ['', [Validators.required, Validators.minLength(6)]],
        confirmPassword: ['', [Validators.required]],
      },
      { validators: this.passwordMatchValidator },
    );
  }

  ngOnInit(): void {
    this.user = this.authService.getUser();

    if (!this.authService.isLoggedIn()) {
      this.navigationService.navigateToLogin();
      return;
    }

    if (this.user) {
      this.profileForm.patchValue({
        username: this.user.username,
        email: this.user.email,
      });
    }
    this.testAuthentication();
  }

  testAuthentication(): void {
    this.authService.testAuth().subscribe({
      next: (response: any) => {},
      error: (error: any) => {},
    });
  }

  passwordMatchValidator(form: FormGroup) {
    const newPassword = form.get('newPassword');
    const confirmPassword = form.get('confirmPassword');

    if (
      newPassword &&
      confirmPassword &&
      newPassword.value !== confirmPassword.value
    ) {
      confirmPassword.setErrors({ passwordMismatch: true });
      return { passwordMismatch: true };
    }

    if (confirmPassword && confirmPassword.errors?.['passwordMismatch']) {
      delete confirmPassword.errors['passwordMismatch'];
      if (Object.keys(confirmPassword.errors).length === 0) {
        confirmPassword.setErrors(null);
      }
    }

    return null;
  }

  updateProfile(): void {
    if (this.profileForm.valid) {
      this.loading = true;
      this.profileSuccessMessage = '';
      this.profileErrorMessage = '';
      const updateData: UpdateProfileRequest = {
        username: this.profileForm.value.username,
        email: this.profileForm.value.email,
      };
      this.authService.updateProfile(updateData).subscribe({
        next: (response) => {
          this.authService.updateLocalUserData(response.user);
          this.user = response.user;
          this.profileSuccessMessage = 'Profil mis à jour avec succès !';
          this.loading = false;
          setTimeout(() => {
            this.profileSuccessMessage = '';
          }, 3000);
        },
        error: (error) => {
          this.loading = false;
          if (error.status === 409) {
            this.profileErrorMessage =
              "Ce nom d'utilisateur ou cet email est déjà utilisé";
          } else if (error.status === 400) {
            this.profileErrorMessage =
              'Données invalides. Veuillez vérifier vos informations.';
          } else if (error.status === 401) {
            this.profileErrorMessage =
              'Non authentifié. Veuillez vous reconnecter.';
          } else {
            this.profileErrorMessage =
              'Une erreur est survenue. Veuillez réessayer.';
          }
        },
      });
    }
  }

  changePassword(): void {
    if (this.passwordForm.valid) {
      this.loading = true;
      this.passwordSuccessMessage = '';
      this.passwordErrorMessage = '';
      const passwordData: ChangePasswordRequest = {
        currentPassword: this.passwordForm.value.currentPassword,
        newPassword: this.passwordForm.value.newPassword,
      };
      this.authService.changePassword(passwordData).subscribe({
        next: (response) => {
          this.passwordSuccessMessage = 'Mot de passe modifié avec succès !';
          this.loading = false;
          this.passwordForm.reset();
          setTimeout(() => {
            this.passwordSuccessMessage = '';
          }, 3000);
        },
        error: (error) => {
          this.loading = false;
          if (error.status === 400) {
            this.passwordErrorMessage = 'Mot de passe actuel incorrect';
          } else if (error.status === 401) {
            this.passwordErrorMessage =
              'Non authentifié. Veuillez vous reconnecter.';
          } else {
            this.passwordErrorMessage =
              'Une erreur est survenue. Veuillez réessayer.';
          }
        },
      });
    }
  }
}
