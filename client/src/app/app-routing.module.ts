import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { UserDashboardComponent } from './components/user-dashboard/user-dashboard.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { SettingsComponent } from './components/settings/settings.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { MovieSearchComponent } from './components/movie-search/movie-search.component';
import { MovieListsComponent } from './components/movie-lists/movie-lists.component';
import { UserStatsComponent } from './components/user-stats/user-stats.component';
import { SystemConfigComponent } from './components/system-config/system-config.component';

const routes: Routes = [
  { path: '', component: HomeComponent },
  { path: 'login', component: LoginComponent },
  { path: 'register', component: RegisterComponent },
  { path: 'dashboard', component: UserDashboardComponent },
  { path: 'user-dashboard', component: UserDashboardComponent },
  { path: 'admin-dashboard', component: AdminDashboardComponent },
  { path: 'settings', component: SettingsComponent },
  { path: 'user-management', component: UserManagementComponent },
  { path: 'admin/users', component: UserManagementComponent },
  { path: 'movies', component: MovieSearchComponent },
  { path: 'lists', component: MovieListsComponent },
  { path: 'stats', component: UserStatsComponent },
  { path: 'system-config', component: SystemConfigComponent },
  { path: '**', redirectTo: '' },
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule],
})
export class AppRoutingModule {}
