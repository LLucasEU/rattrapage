import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface MovieFilters {
  minRating: number;
  maxRating: number;
  startDate: string;
  endDate: string;
  sortBy: 'title' | 'rating' | 'date' | 'popularity';
  sortOrder: 'asc' | 'desc';
}

@Component({
  selector: 'app-movie-filters',
  templateUrl: './movie-filters.component.html',
  styleUrls: ['./movie-filters.component.scss'],
})
export class MovieFiltersComponent {
  @Input() showFilters = false;
  @Output() filtersChanged = new EventEmitter<MovieFilters>();
  @Output() filtersCleared = new EventEmitter<void>();

  filters: MovieFilters = {
    minRating: 0,
    maxRating: 10,
    startDate: '',
    endDate: '',
    sortBy: 'popularity',
    sortOrder: 'desc',
  };

  ratingOptions = [
    { value: 0, label: 'Toutes les notes' },
    { value: 1, label: '1+ étoiles' },
    { value: 2, label: '2+ étoiles' },
    { value: 3, label: '3+ étoiles' },
    { value: 4, label: '4+ étoiles' },
    { value: 5, label: '5+ étoiles' },
    { value: 6, label: '6+ étoiles' },
    { value: 7, label: '7+ étoiles' },
    { value: 8, label: '8+ étoiles' },
    { value: 9, label: '9+ étoiles' },
  ];

  sortOptions = [
    { value: 'popularity', label: 'Popularité' },
    { value: 'rating', label: 'Note' },
    { value: 'date', label: 'Date de sortie' },
    { value: 'title', label: 'Titre' },
  ];

  toggleFilters(): void {
    this.showFilters = !this.showFilters;
  }

  onFilterChange(): void {
    this.filtersChanged.emit(this.filters);
  }

  clearFilters(): void {
    this.filters = {
      minRating: 0,
      maxRating: 10,
      startDate: '',
      endDate: '',
      sortBy: 'popularity',
      sortOrder: 'desc',
    };
    this.filtersCleared.emit();
    this.onFilterChange();
  }

  getActiveFiltersCount(): number {
    let count = 0;
    if (this.filters.minRating > 0) count++;
    if (this.filters.startDate) count++;
    if (this.filters.endDate) count++;
    return count;
  }
}
