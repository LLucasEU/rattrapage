import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { Observable } from 'rxjs';
import { AuthService } from './auth.service';

export interface Movie {
  id: number;
  tmdbId: number;
  title: string;
  overview: string;
  posterPath: string;
  backdropPath: string;
  releaseDate: string;
  voteAverage: number;
  voteCount: number;
  originalLanguage: string;
  popularity: number;
  genres?: string[];
}

export interface UserMovieList {
  id: number;
  movie: Movie;
  listType: 'WATCHED' | 'WATCHLIST';
  addedDate: string;
  watchedDate?: string;
  userRating?: number;
  userNotes?: string;
}

export interface MovieSearchResponse {
  movies: Movie[];
}

export interface UserStats {
  watchlistCount: number;
  watchedCount: number;
}

@Injectable({
  providedIn: 'root',
})
export class MovieService {
  private baseUrl = 'http://localhost:8080/api/v1/movies';

  constructor(
    private http: HttpClient,
    private authService: AuthService,
  ) {}

  private getAuthHeaders(): HttpHeaders {
    const token = this.authService.getToken();
    return new HttpHeaders({
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
    });
  }

  searchMovies(
    query: string,
    page: number = 1,
  ): Observable<MovieSearchResponse> {
    return this.http.get<MovieSearchResponse>(
      `${this.baseUrl}/search?query=${encodeURIComponent(query)}&page=${page}`,
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  getPopularMovies(page: number = 1): Observable<MovieSearchResponse> {
    return this.http.get<MovieSearchResponse>(
      `${this.baseUrl}/popular?page=${page}`,
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  getUserWatchlist(): Observable<{ watchlist: UserMovieList[] }> {
    return this.http.get<{ watchlist: UserMovieList[] }>(
      `${this.baseUrl}/watchlist`,
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  getUserWatchedMovies(): Observable<{ watched: UserMovieList[] }> {
    return this.http.get<{ watched: UserMovieList[] }>(
      `${this.baseUrl}/watched`,
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  addToWatchlist(tmdbId: number): Observable<any> {
    return this.http.post(
      `${this.baseUrl}/${tmdbId}/watchlist`,
      {},
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  markAsWatched(
    tmdbId: number,
    watchedData?: {
      watchedDate: string;
      personalRating: number;
      comment: string;
    },
  ): Observable<any> {
    const body = watchedData
      ? {
          watchedDate: watchedData.watchedDate,
          personalRating: watchedData.personalRating,
          comment: watchedData.comment,
        }
      : {};

    return this.http.post(`${this.baseUrl}/${tmdbId}/watched`, body, {
      headers: this.getAuthHeaders(),
    });
  }

  removeFromWatchlist(tmdbId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${tmdbId}/watchlist`, {
      headers: this.getAuthHeaders(),
    });
  }

  removeFromWatched(tmdbId: number): Observable<any> {
    return this.http.delete(`${this.baseUrl}/${tmdbId}/watched`, {
      headers: this.getAuthHeaders(),
    });
  }

  updateRating(tmdbId: number, rating: number, notes: string): Observable<any> {
    return this.http.put(
      `${this.baseUrl}/${tmdbId}/rating`,
      {
        rating: rating,
        notes: notes,
      },
      {
        headers: this.getAuthHeaders(),
      },
    );
  }

  getUserStats(): Observable<UserStats> {
    return this.http.get<UserStats>(`${this.baseUrl}/stats`, {
      headers: this.getAuthHeaders(),
    });
  }

  exportMovieLists(): Observable<Blob> {
    return this.http.get(`${this.baseUrl}/export`, {
      headers: this.getAuthHeaders(),
      responseType: 'blob',
    });
  }

  getPosterUrl(posterPath: string): string {
    if (!posterPath) {
      return 'assets/images/no-poster.jpg';
    }
    return `https://image.tmdb.org/t/p/w500${posterPath}`;
  }

  getBackdropUrl(backdropPath: string): string {
    if (!backdropPath) {
      return 'assets/images/no-backdrop.jpg';
    }
    return `https://image.tmdb.org/t/p/original${backdropPath}`;
  }
}
