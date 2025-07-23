import { Component, OnInit } from '@angular/core';

@Component({
  selector: 'app-layout',
  templateUrl: './layout.component.html',
  styleUrls: ['./layout.component.scss'],
})
export class LayoutComponent implements OnInit {
  showNavbar = false;

  constructor() {}

  ngOnInit(): void {
    this.showNavbar = true;
  }

  shouldShowNavbar(): boolean {
    const currentPath = window.location.pathname;
    return (
      !currentPath.includes('/login') && !currentPath.includes('/register')
    );
  }
}
