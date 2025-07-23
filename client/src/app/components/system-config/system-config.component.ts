import { Component, HostBinding, OnInit } from '@angular/core';

interface ConfigState {
  fontSize: 'normal' | 'large' | 'xlarge';
  darkMode: boolean;
  colorBlindMode?: boolean;
  dyslexiaFont?: boolean;
  letterSpacing?: 'normal' | 'wide' | 'extra-wide';
  lineSpacing?: 'normal' | 'wide' | 'extra-wide';
}

@Component({
  selector: 'app-system-config',
  templateUrl: './system-config.component.html',
  styleUrls: ['./system-config.component.scss']
})
export class SystemConfigComponent implements OnInit {
  formState: ConfigState = {
    fontSize: 'normal',
    darkMode: false,
    colorBlindMode: false,
    dyslexiaFont: false,
    letterSpacing: 'normal',
    lineSpacing: 'normal'
  };
  successMessage = '';

  ngOnInit(): void {
    const saved = localStorage.getItem('rattrapageConfig');
    if (saved) {
      this.formState = { ...this.formState, ...JSON.parse(saved) };
    }
    this.applyPreferences();
  }

  savePreferences(): void {
    localStorage.setItem('rattrapageConfig', JSON.stringify(this.formState));
    this.applyPreferences();
    this.successMessage = 'Préférences enregistrées !';
    setTimeout(() => (this.successMessage = ''), 2500);
  }

  applyPreferences(): void {
    if (this.formState.fontSize === 'large') {
      document.body.style.fontSize = '1.25em';
    } else if (this.formState.fontSize === 'xlarge') {
      document.body.style.fontSize = '1.5em';
    } else {
      document.body.style.fontSize = '';
    }
    if (this.formState.darkMode) {
      document.body.classList.add('dark-mode');
    } else {
      document.body.classList.remove('dark-mode');
    }
    if (this.formState.colorBlindMode) {
      document.body.classList.add('color-blind');
    } else {
      document.body.classList.remove('color-blind');
    }
    if (this.formState.dyslexiaFont) {
      document.body.classList.add('dyslexia-font');
    } else {
      document.body.classList.remove('dyslexia-font');
    }
    if (this.formState.letterSpacing === 'wide') {
      document.body.classList.add('letter-spacing-wide');
    } else if (this.formState.letterSpacing === 'extra-wide') {
      document.body.classList.add('letter-spacing-extra-wide');
    } else {
      document.body.classList.remove('letter-spacing-wide', 'letter-spacing-extra-wide');
    }
    if (this.formState.lineSpacing === 'wide') {
      document.body.classList.add('line-spacing-wide');
    } else if (this.formState.lineSpacing === 'extra-wide') {
      document.body.classList.add('line-spacing-extra-wide');
    } else {
      document.body.classList.remove('line-spacing-wide', 'line-spacing-extra-wide');
    }
  }
}
