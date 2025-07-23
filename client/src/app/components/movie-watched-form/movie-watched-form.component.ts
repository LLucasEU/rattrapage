import { Component, Input, Output, EventEmitter } from '@angular/core';
import { Movie } from '../../services/movie.service';

export interface WatchedMovieData {
  tmdbId: number;
  watchedDate: string;
  personalRating: number;
  comment: string;
}

@Component({
  selector: 'app-movie-watched-form',
  templateUrl: './movie-watched-form.component.html',
  styleUrls: ['./movie-watched-form.component.scss'],
})
export class MovieWatchedFormComponent {
  @Input() movie: Movie | null = null;
  @Input() showModal = false;
  @Output() submitForm = new EventEmitter<WatchedMovieData>();
  @Output() closeModal = new EventEmitter<void>();

  formData: WatchedMovieData = {
    tmdbId: 0,
    watchedDate: new Date().toISOString().split('T')[0],
    personalRating: 0,
    comment: '',
  };

  ratingOptions = [
    { value: 0, label: 'Non noté' },
    { value: 1, label: '⭐ 1/10' },
    { value: 2, label: '⭐⭐ 2/10' },
    { value: 3, label: '⭐⭐⭐ 3/10' },
    { value: 4, label: '⭐⭐⭐⭐ 4/10' },
    { value: 5, label: '⭐⭐⭐⭐⭐ 5/10' },
    { value: 6, label: '⭐⭐⭐⭐⭐⭐ 6/10' },
    { value: 7, label: '⭐⭐⭐⭐⭐⭐⭐ 7/10' },
    { value: 8, label: '⭐⭐⭐⭐⭐⭐⭐⭐ 8/10' },
    { value: 9, label: '⭐⭐⭐⭐⭐⭐⭐⭐⭐ 9/10' },
    { value: 10, label: '⭐⭐⭐⭐⭐⭐⭐⭐⭐⭐ 10/10' },
  ];

  onSubmit(): void {
    if (this.movie) {
      this.formData.tmdbId = this.movie.tmdbId;
      this.submitForm.emit(this.formData);
      this.resetForm();
    }
  }

  onClose(): void {
    this.closeModal.emit();
    this.resetForm();
  }

  private resetForm(): void {
    this.formData = {
      tmdbId: 0,
      watchedDate: new Date().toISOString().split('T')[0],
      personalRating: 0,
      comment: '',
    };
  }

  onBackdropClick(event: Event): void {
    if (event.target === event.currentTarget) {
      this.onClose();
    }
  }

  getReleaseYear(releaseDate: string): number {
    if (!releaseDate) return 0;
    return new Date(releaseDate).getFullYear();
  }
}
