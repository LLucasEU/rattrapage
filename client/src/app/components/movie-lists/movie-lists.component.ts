import { Component, OnInit } from '@angular/core';
import { MovieService, UserMovieList } from '../../services/movie.service';
import { AuthService } from '../../services/auth.service';
import { NavigationService } from '../../services/navigation.service';
import { MovieFilters } from '../movie-filters/movie-filters.component';
import { WatchedMovieData } from '../movie-watched-form/movie-watched-form.component';

@Component({
  selector: 'app-movie-lists',
  templateUrl: './movie-lists.component.html',
  styleUrls: ['./movie-lists.component.scss'],
})
export class MovieListsComponent implements OnInit {
  watchlist: UserMovieList[] = [];
  watchedMovies: UserMovieList[] = [];
  activeTab: 'watchlist' | 'watched' = 'watchlist';
  loading = false;
  errorMessage = '';
  successMessage = '';

  showFilters = false;
  currentFilters: MovieFilters = {
    minRating: 0,
    maxRating: 10,
    startDate: '',
    endDate: '',
    sortBy: 'popularity',
    sortOrder: 'desc',
  };

  filteredWatchlist: UserMovieList[] = [];
  filteredWatchedMovies: UserMovieList[] = [];

  selectedMovie: any = null;
  showWatchedForm = false;

  viewMode: 'cards' | 'table' = 'cards';
  isTableViewAvailable: boolean = true;
  showTableTooltip: boolean = false;

  tableSort: { column: string; direction: 'asc' | 'desc' } = {
    column: '',
    direction: 'asc',
  };

  public Math = Math;

  tooltipMovie: UserMovieList | null = null;

  showCommentTooltip(movie: UserMovieList) {
    this.tooltipMovie = movie;
    setTimeout(() => {
      document.addEventListener('click', this.hideTooltipOnClickOutside, {
        once: true,
      });
    });
  }

  hideCommentTooltip(movie?: UserMovieList) {
    this.tooltipMovie = null;
  }

  private hideTooltipOnClickOutside = (event: MouseEvent) => {
    const tooltip = document.querySelector('.custom-tooltip');
    if (tooltip && !tooltip.contains(event.target as Node)) {
      this.tooltipMovie = null;
    }
  };

  constructor(
    private movieService: MovieService,
    private authService: AuthService,
    private navigationService: NavigationService,
  ) {}

  ngOnInit(): void {
    this.isTableViewAvailable = window.innerWidth >= 900;
    window.addEventListener('resize', this.onResize.bind(this));
    if (!this.authService.isLoggedIn()) {
      this.navigationService.navigateToLogin();
      return;
    }

    this.filteredWatchlist = [];
    this.filteredWatchedMovies = [];

    this.loadLists();
  }

  onResize() {
    this.isTableViewAvailable = window.innerWidth >= 900;
    if (!this.isTableViewAvailable && this.viewMode === 'table') {
      this.setViewMode('cards');
    }
  }

  loadLists(): void {
    this.loading = true;
    this.errorMessage = '';

    Promise.all([
      this.movieService.getUserWatchlist().toPromise(),
      this.movieService.getUserWatchedMovies().toPromise(),
    ])
      .then(([watchlistResponse, watchedResponse]) => {
        this.watchlist = watchlistResponse?.watchlist || [];
        this.watchedMovies = watchedResponse?.watched || [];
        this.filteredWatchlist = [...this.watchlist];
        this.filteredWatchedMovies = [...this.watchedMovies];
        this.loading = false;
      })
      .catch((error) => {
        this.errorMessage = 'Erreur lors du chargement des listes';
        this.loading = false;
      });
  }

  setActiveTab(tab: 'watchlist' | 'watched'): void {
    this.activeTab = tab;
  }

  removeFromWatchlist(movie: UserMovieList): void {
    this.movieService.removeFromWatchlist(movie.movie.tmdbId).subscribe({
      next: () => {
        this.watchlist = this.watchlist.filter((item) => item.id !== movie.id);
        this.applyFilters();
        this.successMessage = `"${movie.movie.title}" retiré de votre liste à voir`;
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);

        this.notifyMovieStateChange(movie.movie.tmdbId, 'watchlist', false);
      },
      error: (error) => {
        console.error('Error removing from watchlist:', error);
        this.errorMessage = 'Erreur lors de la suppression';
      },
    });
  }

  removeFromWatched(movie: UserMovieList): void {
    this.movieService.removeFromWatched(movie.movie.tmdbId).subscribe({
      next: () => {
        this.watchedMovies = this.watchedMovies.filter(
          (item) => item.id !== movie.id,
        );
        this.applyFilters();
        this.successMessage = `"${movie.movie.title}" retiré de vos films vus`;
        setTimeout(() => {
          this.successMessage = '';
        }, 3000);

        this.notifyMovieStateChange(movie.movie.tmdbId, 'watched', false);
      },
      error: (error) => {
        console.error('Error removing from watched:', error);
        this.errorMessage = 'Erreur lors de la suppression';
      },
    });
  }

  markAsWatched(movie: UserMovieList): void {
    this.selectedMovie = movie.movie;
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
          this.watchlist = this.watchlist.filter(
            (item) => item.id !== this.selectedMovie?.id,
          );
          this.watchedMovies.unshift({
            id: Date.now(),
            listType: 'WATCHED',
            addedDate: new Date().toISOString(),
            watchedDate: data.watchedDate,
            movie: this.selectedMovie,
            userRating: data.personalRating,
            userNotes: data.comment,
          });
          this.successMessage = `"${this.selectedMovie?.title}" marqué comme vu !`;
          setTimeout(() => {
            this.successMessage = '';
          }, 3000);
          this.closeWatchedForm();
          this.applyFilters();

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

  formatDate(dateString: string): string {
    if (!dateString) return 'Date inconnue';
    const date = new Date(dateString);
    return date.toLocaleDateString('fr-FR');
  }

  getRatingColor(rating: number): string {
    if (rating >= 8) return '#4CAF50';
    if (rating >= 6) return '#FF9800';
    return '#F44336';
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
    this.filteredWatchlist = [...this.watchlist];
    this.filteredWatchedMovies = [...this.watchedMovies];
  }

  private applyFilters(): void {
    this.filteredWatchlist = this.filterMovies(this.watchlist);

    this.filteredWatchedMovies = this.filterMovies(this.watchedMovies);
  }

  private filterMovies(movies: UserMovieList[]): UserMovieList[] {
    let filteredMovies = [...movies];

    if (this.currentFilters.minRating > 0) {
      filteredMovies = filteredMovies.filter(
        (movie) => movie.movie.voteAverage >= this.currentFilters.minRating,
      );
    }

    if (this.currentFilters.startDate) {
      const startDate = new Date(this.currentFilters.startDate);
      filteredMovies = filteredMovies.filter((movie) => {
        if (!movie.movie.releaseDate) return false;
        const movieDate = new Date(movie.movie.releaseDate);
        return movieDate >= startDate;
      });
    }

    if (this.currentFilters.endDate) {
      const endDate = new Date(this.currentFilters.endDate);
      filteredMovies = filteredMovies.filter((movie) => {
        if (!movie.movie.releaseDate) return false;
        const movieDate = new Date(movie.movie.releaseDate);
        return movieDate <= endDate;
      });
    }

    filteredMovies.sort((a, b) => {
      let aValue: any, bValue: any;

      switch (this.currentFilters.sortBy) {
        case 'title':
          aValue = a.movie.title?.toLowerCase() || '';
          bValue = b.movie.title?.toLowerCase() || '';
          break;
        case 'rating':
          aValue = a.movie.voteAverage || 0;
          bValue = b.movie.voteAverage || 0;
          break;
        case 'date':
          aValue = a.movie.releaseDate
            ? new Date(a.movie.releaseDate).getTime()
            : 0;
          bValue = b.movie.releaseDate
            ? new Date(b.movie.releaseDate).getTime()
            : 0;
          break;
        case 'popularity':
        default:
          aValue = a.movie.popularity || 0;
          bValue = b.movie.popularity || 0;
          break;
      }

      if (this.currentFilters.sortOrder === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    });

    return filteredMovies;
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

  setViewMode(mode: 'cards' | 'table') {
    if (mode === 'table' && !this.isTableViewAvailable) {
      this.showTableTooltip = true;
      setTimeout(() => (this.showTableTooltip = false), 2500);
      return;
    }
    this.viewMode = mode;
  }

  sortTable(column: string) {
    if (this.tableSort.column === column) {
      this.tableSort.direction =
        this.tableSort.direction === 'asc' ? 'desc' : 'asc';
    } else {
      this.tableSort.column = column;
      this.tableSort.direction = 'asc';
    }
    this.applyTableSort();
  }

  private applyTableSort() {
    const sortCol = this.tableSort.column;
    const dir = this.tableSort.direction;
    const sortFn = (a: UserMovieList, b: UserMovieList) => {
      let aValue: any = '';
      let bValue: any = '';
      switch (sortCol) {
        case 'title':
          aValue = a.movie.title?.toLowerCase() || '';
          bValue = b.movie.title?.toLowerCase() || '';
          break;
        case 'releaseDate':
          aValue = a.movie.releaseDate || '';
          bValue = b.movie.releaseDate || '';
          break;
        case 'voteAverage':
          aValue = Number(a.movie.voteAverage) || 0;
          bValue = Number(b.movie.voteAverage) || 0;
          break;
        case 'userRating':
          aValue = Number(a.userRating) || 0;
          bValue = Number(b.userRating) || 0;
          break;
        case 'addedDate':
          aValue = a.addedDate || '';
          bValue = b.addedDate || '';
          break;
        case 'watchedDate':
          aValue = a.watchedDate || '';
          bValue = b.watchedDate || '';
          break;
        default:
          aValue = '';
          bValue = '';
      }
      if (dir === 'asc') {
        return aValue > bValue ? 1 : -1;
      } else {
        return aValue < bValue ? 1 : -1;
      }
    };
    if (this.activeTab === 'watchlist') {
      this.filteredWatchlist = [...this.filteredWatchlist].sort(sortFn);
    } else {
      this.filteredWatchedMovies = [...this.filteredWatchedMovies].sort(sortFn);
    }
  }
}
