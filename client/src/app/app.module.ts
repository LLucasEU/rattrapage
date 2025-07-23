import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { HomeComponent } from './components/home/home.component';
import { LoginComponent } from './components/login/login.component';
import { RegisterComponent } from './components/register/register.component';
import { UserDashboardComponent } from './components/user-dashboard/user-dashboard.component';
import { AdminDashboardComponent } from './components/admin-dashboard/admin-dashboard.component';
import { SettingsComponent } from './components/settings/settings.component';
import { UserManagementComponent } from './components/user-management/user-management.component';
import { MovieSearchComponent } from './components/movie-search/movie-search.component';
import { MovieListsComponent } from './components/movie-lists/movie-lists.component';
import { MovieFiltersComponent } from './components/movie-filters/movie-filters.component';
import { MovieWatchedFormComponent } from './components/movie-watched-form/movie-watched-form.component';
import { UserStatsComponent } from './components/user-stats/user-stats.component';
import { NavbarComponent } from './components/navbar/navbar.component';
import { LayoutComponent } from './components/layout/layout.component';
import { NgChartsModule } from 'ng2-charts';
import { SystemConfigComponent } from './components/system-config/system-config.component';

@NgModule({
  declarations: [
    AppComponent,
    HomeComponent,
    LoginComponent,
    RegisterComponent,
    UserDashboardComponent,
    AdminDashboardComponent,
    SettingsComponent,
    UserManagementComponent,
    MovieSearchComponent,
    MovieListsComponent,
    MovieFiltersComponent,
    MovieWatchedFormComponent,
    UserStatsComponent,
    NavbarComponent,
    LayoutComponent,
    SystemConfigComponent,
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    NgChartsModule,
  ],
  providers: [],
  bootstrap: [AppComponent],
})
export class AppModule {}
