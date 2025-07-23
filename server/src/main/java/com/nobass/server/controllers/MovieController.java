package com.nobass.server.controllers;

import com.nobass.server.entities.Movie;
import com.nobass.server.entities.UserMovieList;
import com.nobass.server.enums.ListType;
import com.nobass.server.services.AuthService;
import com.nobass.server.services.TmdbService;
import com.nobass.server.services.UserMovieListService;
import com.nobass.server.utils.ExcelExportUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contrôleur REST pour la gestion des films et des listes utilisateur.
 * <p>
 * Permet la recherche, la consultation, l'ajout, la suppression, la notation et l'export des films pour un utilisateur connecté.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/movies")
@CrossOrigin(origins = "http://localhost:4200")
public class MovieController {

    @Autowired
    private TmdbService tmdbService;

    @Autowired
    private UserMovieListService userMovieListService;

    @Autowired
    private AuthService authService;

    /**
     * Recherche des films par titre (via l'API TMDB).
     * @param query Titre ou mot-clé à rechercher
     * @param page Numéro de page (défaut : 1)
     * @return Liste des films correspondants (DTO)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchMovies(@RequestParam String query, @RequestParam(defaultValue = "1") int page) {
        try {
            List<Movie> movies = tmdbService.searchMovies(query, page);
            List<Map<String, Object>> movieDtos = movies.stream()
                .map(this::convertToDto)
                .toList();

            return ResponseEntity.ok(Map.of("movies", movieDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère les films populaires (via l'API TMDB).
     * @param page Numéro de page (défaut : 1)
     * @return Liste des films populaires (DTO)
     */
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularMovies(@RequestParam(defaultValue = "1") int page) {
        try {
            List<Movie> movies = tmdbService.getPopularMovies(page);

            List<Map<String, Object>> movieDtos = movies.stream()
                .map(this::convertToDto)
                .toList();

            return ResponseEntity.ok(Map.of("movies", movieDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère la liste "à voir" de l'utilisateur connecté.
     * @return Liste des films à voir (DTO)
     */
    @GetMapping("/watchlist")
    public ResponseEntity<?> getUserWatchlist() {
        try {

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();
            List<UserMovieList> watchlist = userMovieListService.getUserWatchlist(userId);

            List<Map<String, Object>> watchlistDtos = watchlist.stream()
                .map(this::convertUserMovieListToDto)
                .toList();

            return ResponseEntity.ok(Map.of("watchlist", watchlistDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Récupère la liste des films vus de l'utilisateur connecté.
     * @return Liste des films vus (DTO)
     */
    @GetMapping("/watched")
    public ResponseEntity<?> getUserWatchedMovies() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }
            Long userId = authService.getCurrentUserId();
            List<UserMovieList> watchedMovies = userMovieListService.getUserWatchedMovies(userId);

            List<Map<String, Object>> watchedDtos = watchedMovies.stream()
                .map(this::convertUserMovieListToDto)
                .toList();

            return ResponseEntity.ok(Map.of("watched", watchedDtos));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Ajoute un film à la liste "à voir" de l'utilisateur connecté.
     * @param tmdbId Identifiant TMDB du film
     * @return Confirmation et détail du film ajouté
     */
    @PostMapping("/{tmdbId}/watchlist")
    public ResponseEntity<?> addToWatchlist(@PathVariable Long tmdbId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();
            UserMovieList userMovieList = userMovieListService.addToWatchlist(userId, tmdbId);

            return ResponseEntity.ok(Map.of(
                "message", "Film ajouté à votre liste à voir",
                "userMovieList", convertUserMovieListToDto(userMovieList)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Marque un film comme vu pour l'utilisateur connecté.
     * @param tmdbId Identifiant TMDB du film
     * @param request Données optionnelles : date de visionnage, note, commentaire
     * @return Confirmation et détail du film marqué comme vu
     */
    @PostMapping("/{tmdbId}/watched")
    public ResponseEntity<?> markAsWatched(@PathVariable Long tmdbId, @RequestBody(required = false) Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();

            String watchedDate = null;
            Integer personalRating = null;
            String comment = null;

            if (request != null) {
                watchedDate = (String) request.get("watchedDate");

                Object personalRatingObj = request.get("personalRating");
                personalRating = getRating(personalRatingObj, personalRating);

                comment = (String) request.get("comment");
            }

            UserMovieList userMovieList = userMovieListService.markAsWatched(userId, tmdbId, watchedDate, personalRating, comment);

            return ResponseEntity.ok(Map.of(
                "message", "Film marqué comme vu",
                "userMovieList", convertUserMovieListToDto(userMovieList)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retire un film de la liste "à voir" de l'utilisateur connecté.
     * @param tmdbId Identifiant TMDB du film
     * @return Confirmation de la suppression
     */
    @DeleteMapping("/{tmdbId}/watchlist")
    public ResponseEntity<?> removeFromWatchlist(@PathVariable Long tmdbId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();
            userMovieListService.removeFromList(userId, tmdbId, ListType.WATCHLIST);

            return ResponseEntity.ok(Map.of("message", "Film retiré de votre liste à voir"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Retire un film de la liste des films vus de l'utilisateur connecté.
     * @param tmdbId Identifiant TMDB du film
     * @return Confirmation de la suppression
     */
    @DeleteMapping("/{tmdbId}/watched")
    public ResponseEntity<?> removeFromWatched(@PathVariable Long tmdbId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();
            userMovieListService.removeFromList(userId, tmdbId, ListType.WATCHED);

            return ResponseEntity.ok(Map.of("message", "Film retiré de votre liste des films vus"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Met à jour la note et le commentaire d'un film vu par l'utilisateur connecté.
     * @param tmdbId Identifiant TMDB du film
     * @param request Données : note (rating), commentaire (notes)
     * @return Confirmation et détail mis à jour
     */
    @PutMapping("/{tmdbId}/rating")
    public ResponseEntity<?> updateRating(@PathVariable Long tmdbId, @RequestBody Map<String, Object> request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();

            // Gérer la conversion sécurisée de rating
            Object ratingObj = request.get("rating");
            Integer rating = null;
            rating = getRating(ratingObj, rating);

            String notes = (String) request.get("notes");

            UserMovieList userMovieList = userMovieListService.updateUserRating(userId, tmdbId, rating, notes);

            return ResponseEntity.ok(Map.of(
                "message", "Note mise à jour",
                "userMovieList", convertUserMovieListToDto(userMovieList)
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    private static Integer getRating(Object ratingObj, Integer rating) {
        if (ratingObj != null) {
            if (ratingObj instanceof Integer) {
                rating = (Integer) ratingObj;
            } else if (ratingObj instanceof String) {
                try {
                    rating = Integer.parseInt((String) ratingObj);
                } catch (NumberFormatException e) {
                    rating = null;
                }
            } else if (ratingObj instanceof Number) {
                rating = ((Number) ratingObj).intValue();
            }
        }
        return rating;
    }

    /**
     * Récupère les statistiques de l'utilisateur connecté (nombre de films à voir et vus).
     * @return Statistiques (watchlistCount, watchedCount)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getUserStats() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }

            Long userId = authService.getCurrentUserId();
            long watchlistCount = userMovieListService.getWatchlistCount(userId);
            long watchedCount = userMovieListService.getWatchedCount(userId);

            return ResponseEntity.ok(Map.of(
                "watchlistCount", watchlistCount,
                "watchedCount", watchedCount
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Exporte les listes de films de l'utilisateur connecté au format Excel.
     * @return Fichier Excel (.xlsx) contenant les listes "à voir" et "vus"
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportMovieListsExcel() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null) {
                return ResponseEntity.status(401).body(Map.of("error", "Non authentifié"));
            }
            Long userId = authService.getCurrentUserId();
            var watched = userMovieListService.getUserWatchedMovies(userId);
            var watchlist = userMovieListService.getUserWatchlist(userId);
            ByteArrayOutputStream out = ExcelExportUtil.exportMovieListsToExcel(watched, watchlist);
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=mes-films.xlsx");
            headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(out.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Erreur lors de l'export : " + e.getMessage()));
        }
    }

    private Map<String, Object> convertToDto(Movie movie) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", movie.getId());
        dto.put("tmdbId", movie.getTmdbId());
        dto.put("title", movie.getTitle());
        dto.put("overview", movie.getOverview());
        dto.put("posterPath", movie.getPosterPath());
        dto.put("backdropPath", movie.getBackdropPath());
        dto.put("releaseDate", movie.getReleaseDate());
        dto.put("voteAverage", movie.getVoteAverage());
        dto.put("voteCount", movie.getVoteCount());
        dto.put("originalLanguage", movie.getOriginalLanguage());
        dto.put("popularity", movie.getPopularity());
        dto.put("genres", movie.getGenres());
        return dto;
    }

    private Map<String, Object> convertUserMovieListToDto(UserMovieList userMovieList) {
        Map<String, Object> dto = new HashMap<>();
        dto.put("id", userMovieList.getId());
        dto.put("movie", convertToDto(userMovieList.getMovie()));
        dto.put("listType", userMovieList.getListType().name());
        dto.put("addedDate", userMovieList.getAddedDate());
        dto.put("watchedDate", userMovieList.getWatchedDate());
        dto.put("userRating", userMovieList.getUserRating());
        dto.put("userNotes", userMovieList.getUserNotes());
        return dto;
    }
}
