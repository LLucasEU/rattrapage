import { Component, OnInit, OnDestroy } from '@angular/core';
import { MovieService, Movie } from '../../services/movie.service';
import { AuthService } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';
import { Router } from '@angular/router';
import { debounceTime, distinctUntilChanged, switchMap } from 'rxjs/operators';
import { Subject } from 'rxjs';
import { MovieFilters } from '../movie-filters/movie-filters.component';
import { WatchedMovieData } from '../movie-watched-form/movie-watched-form.component';

@Component({
  selector: 'app-movie-search',
  templateUrl: './movie-search.component.html',
  styleUrls: ['./movie-search.component.scss'],
})
export class MovieSearchComponent implements OnInit, OnDestroy {
  movies: Movie[] = [];
  searchQuery = '';
  currentPage = 1;
  loading = false;
  errorMessage = '';
  successMessage = '';
  searchSubject = new Subject<string>();

  watchlistStates: { [tmdbId: number]: boolean } = {};
  watchedStates: { [tmdbId: number]: boolean } = {};

  showFilters = false;
  currentFilters: MovieFilters = {
    minRating: 0,
    maxRating: 10,
    startDate: '',
    endDate: '',
    sortBy: 'popularity',
    sortOrder: 'desc',
  };

  selectedMovie: Movie | null = null;
  showWatchedForm = false;

  constructor(
    private movieService: MovieService,
    private authService: AuthService,
    private navigationService: NavigationService,
    private router: Router,
  ) {}

  ngOnInit(): void {
    if (!this.authService.isLoggedIn()) {
      this.navigationService.navigateToLogin();
      return;
    }

    this.searchSubject
      .pipe(
        debounceTime(500),
        distinctUntilChanged(),
        switchMap((query) => {
          this.loading = true;
          this.errorMessage = '';
          return this.movieService.searchMovies(query, 1);
        }),
      )
      .subscribe({
        next: (response) => {
          this.movies = response.movies;
          this.currentPage = 1;
          this.loading = false;
        },
        error: (error) => {
          console.error('Error searching movies:', error);
          this.errorMessage = 'Erreur lors de la recherche';
          this.loading = false;
        },
      });

    this.loadPopularMovies();
    this.loadUserMovieStates();
    this.setupMovieStateListener();
  }

  ngOnDestroy(): void {
    window.removeEventListener(
      'movieStateChanged',
      this.handleMovieStateChange,
    );
  }

  private setupMovieStateListener(): void {
    window.addEventListener('movieStateChanged', this.handleMovieStateChange);
  }

  loadUserMovieStates(): void {
    this.movieService.getUserWatchlist().subscribe({
      next: (response) => {
        response.watchlist.forEach((item) => {
          this.watchlistStates[item.movie.tmdbId] = true;
        });
      },
      error: (error) => {
        console.error('Error loading watchlist:', error);
      },
    });

    this.movieService.getUserWatchedMovies().subscribe({
      next: (response) => {
        response.watched.forEach((item) => {
          this.watchedStates[item.movie.tmdbId] = true;
        });
      },
      error: (error) => {
        console.error('Error loading watched movies:', error);
      },
    });
  }

  onSearch(): void {
    if (this.searchQuery.trim()) {
      this.searchSubject.next(this.searchQuery.trim());
    } else {
      this.loadPopularMovies();
    }
  }

  loadPopularMovies(): void {
    this.loading = true;
    this.errorMessage = '';

    this.movieService.getPopularMovies(this.currentPage).subscribe({
      next: (response) => {
        this.movies = response.movies;
        this.loading = false;
      },
      error: (error) => {
        console.error('Error loading popular movies:', error);
        this.errorMessage = 'Erreur lors du chargement des films populaires';
        this.loading = false;
      },
    });
  }

  addToWatchlist(movie: Movie): void {
    if (this.watchlistStates[movie.tmdbId]) {
      return;
    }

    this.movieService.addToWatchlist(movie.tmdbId).subscribe({
      next: () => {
        this.watchlistStates[movie.tmdbId] = true;
        this.successMessage = `"${movie.title}" ajouté à votre liste à voir !`;
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);

        this.notifyMovieStateChange(movie.tmdbId, 'watchlist', true);
      },
      error: (error) => {
        console.error('Error adding to watchlist:', error);
        if (error.status === 400) {
          this.errorMessage = 'Ce film est déjà dans votre liste à voir';
        } else {
          this.errorMessage = "Erreur lors de l'ajout à la liste à voir";
        }
        setTimeout(() => {
          this.errorMessage = '';
        }, 3000);
      },
    });
  }

  markAsWatched(movie: Movie): void {
    if (this.watchedStates[movie.tmdbId]) {
      this.errorMessage = 'Ce film est déjà marqué comme vu';
      setTimeout(() => {
        this.errorMessage = '';
      }, 3000);
      return;
    }

    this.selectedMovie = movie;
    this.showWatchedForm = true;
  }

  onWatchedFormSubmit(data: WatchedMovieData): void {
    this.movieService
      .markAsWatched(data.tmdbId, {
        watchedDate: data.watchedDate,
        personalRating: data.personalRating,
        comment: data.comment,
      })
      .subscribe({
        next: () => {
          this.watchedStates[data.tmdbId] = true;
          this.successMessage = `"${this.selectedMovie?.title}" marqué comme vu !`;
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
          this.closeWatchedForm();

          this.notifyMovieStateChange(data.tmdbId, 'watched', true);
        },
        error: (error) => {
          console.error('Error marking as watched:', error);
          if (error.status === 400) {
            this.errorMessage = 'Ce film est déjà marqué comme vu';
          } else {
            this.errorMessage = 'Erreur lors du marquage comme vu';
          }
          setTimeout(() => {
            this.errorMessage = '';
          }, 3000);
          this.closeWatchedForm();
        },
      });
  }

  closeWatchedForm(): void {
    this.showWatchedForm = false;
    this.selectedMovie = null;
  }

  getPosterUrl(posterPath: string): string {
    return this.movieService.getPosterUrl(posterPath);
  }

  getRatingColor(rating: number): string {
    if (rating >= 8) return '#4CAF50';
    if (rating >= 6) return '#FF9800';
    return '#F44336';
  }

  formatDate(dateString: string): string {
    if (!dateString) return 'Date inconnue';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
  }

  truncateText(text: string, maxLength: number): string {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
  }

  clearSearch(): void {
    this.searchQuery = '';
    this.loadPopularMovies();
  }

  onFiltersChanged(filters: MovieFilters): void {
    this.currentFilters = filters;
    this.applyFilters();
  }

  onFiltersCleared(): void {
    this.currentFilters = {
      minRating: 0,
      maxRating: 10,
      startDate: '',
      endDate: '',
      sortBy: 'popularity',
      sortOrder: 'desc',
    };
    if (this.searchQuery.trim()) {
      this.onSearch();
    } else {
      this.loadPopularMovies();
    }
  }

  private applyFilters(): void {
    if (this.movies.length === 0) return;

    let filteredMovies = [...this.movies];

    if (this.currentFilters.minRating > 0) {
      filteredMovies = filteredMovies.filter(
        (movie) => movie.voteAverage >= this.currentFilters.minRating,
      );
    }

    if (this.currentFilters.startDate) {
      const startDate = new Date(this.currentFilters.startDate);
      filteredMovies = filteredMovies.filter((movie) => {
        if (!movie.releaseDate) return false;
        const movieDate = new Date(movie.releaseDate);
        return movieDate >= startDate;
      });
    }

    if (this.currentFilters.endDate) {
      const endDate = new Date(this.currentFilters.endDate);
      filteredMovies = filteredMovies.filter((movie) => {
        if (!movie.releaseDate) return false;
        const movieDate = new Date(movie.releaseDate);
        return movieDate <= endDate;
      });
    }

    filteredMovies.sort((a, b) => {
      let aValue: any, bValue: any;

      switch (this.currentFilters.sortBy) {
        case 'title':
          aValue = a.title?.toLowerCase() || '';
          bValue = b.title?.toLowerCase() || '';
          break;
        case 'rating':
          aValue = a.voteAverage || 0;
          bValue = b.voteAverage || 0;
          break;
        case 'date':
          aValue = a.releaseDate ? new Date(a.releaseDate).getTime() : 0;
          bValue = b.releaseDate ? new Date(b.releaseDate).getTime() : 0;
          break;
        case 'popularity':
        default:
          aValue = a.popularity || 0;
          bValue = b.popularity || 0;
          break;
      }

      if (this.currentFilters.sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    this.movies = filteredMovies;
  }

  onImageError(event: Event): void {
    const target = event.target as HTMLImageElement;
    if (target) {
      target.src = 'assets/images/no-poster.jpg';
    }
  }

  isInWatchlist(movie: Movie): boolean {
    return this.watchlistStates[movie.tmdbId] || false;
  }

  isWatched(movie: Movie): boolean {
    return this.watchedStates[movie.tmdbId] || false;
  }

  private notifyMovieStateChange(
    tmdbId: number,
    listType: 'watchlist' | 'watched',
    isAdded: boolean,
  ): void {
    const event = new CustomEvent('movieStateChanged', {
      detail: { tmdbId, listType, isAdded },
    });
    window.dispatchEvent(event);
  }

  private handleMovieStateChange = (event: any) => {
    const { tmdbId, listType, isAdded } = event.detail;

    if (listType === 'watchlist') {
      this.watchlistStates[tmdbId] = isAdded;
    } else if (listType === 'watched') {
      this.watchedStates[tmdbId] = isAdded;
    }
  };
}
