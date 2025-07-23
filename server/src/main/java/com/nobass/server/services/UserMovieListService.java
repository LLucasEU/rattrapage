package com.nobass.server.services;

import com.nobass.server.entities.Movie;
import com.nobass.server.entities.User;
import com.nobass.server.entities.UserMovieList;
import com.nobass.server.enums.ListType;
import com.nobass.server.exceptions.InvalidUserMovieListOperationException;
import com.nobass.server.exceptions.MovieNotFoundException;
import com.nobass.server.exceptions.UserMovieListNotFoundException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.MovieRepository;
import com.nobass.server.repositories.UserMovieListRepository;
import com.nobass.server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static com.nobass.server.enums.ListType.WATCHED;
import static com.nobass.server.enums.ListType.WATCHLIST;

/**
 * Service métier pour la gestion des listes de films utilisateur (watchlist, vus, etc.).
 * <p>
 * Fournit les opérations d'ajout, suppression, notation, export et statistiques sur les listes de films.
 * </p>
 */
@Service
public class UserMovieListService {

    private final UserMovieListRepository userMovieListRepository;
    private final MovieRepository movieRepository;
    private final UserRepository userRepository;
    private final TmdbService tmdbService;

    @Autowired
    public UserMovieListService(UserMovieListRepository userMovieListRepository, MovieRepository movieRepository, UserRepository userRepository, TmdbService tmdbService) {
        this.userMovieListRepository = userMovieListRepository;
        this.movieRepository = movieRepository;
        this.userRepository = userRepository;
        this.tmdbService = tmdbService;
    }

    /**
     * Récupère la watchlist de l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @return Liste des UserMovieList (à voir)
     */
    public List<UserMovieList> getUserWatchlist(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(UserNotFoundException::new);
        return userMovieListRepository.findByUserAndListTypeOrderByAddedDateDesc(user, WATCHLIST);
    }

    /**
     * Récupère la liste des films vus de l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @return Liste des UserMovieList (vus)
     */
    public List<UserMovieList> getUserWatchedMovies(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMovieListRepository.findByUserAndListTypeOrderByAddedDateDesc(user, WATCHED);
    }

    /**
     * Ajoute un film à la watchlist de l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @param tmdbId Identifiant TMDB du film
     * @return UserMovieList ajoutée
     */
    @Transactional
    public UserMovieList addToWatchlist(Long userId, Long tmdbId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Movie movie = getOrCreateMovie(tmdbId);

        if (userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHLIST)) {
            throw new InvalidUserMovieListOperationException("Le film est déjà dans votre liste à voir");
        }

        userMovieListRepository.deleteByUserAndMovieAndListType(user, movie, WATCHED);

        UserMovieList userMovieList = new UserMovieList(user, movie, WATCHLIST);
        return userMovieListRepository.save(userMovieList);
    }

    /**
     * Marque un film comme vu pour l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @param tmdbId Identifiant TMDB du film
     * @param watchedDate Date de visionnage (optionnelle)
     * @param personalRating Note personnelle (optionnelle)
     * @param comment Commentaire (optionnel)
     * @return UserMovieList mise à jour
     */
    @Transactional
    public UserMovieList markAsWatched(Long userId, Long tmdbId, String watchedDate, Integer personalRating, String comment) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Movie movie = getOrCreateMovie(tmdbId);

        if (userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHED)) {
            throw new InvalidUserMovieListOperationException("Le film est déjà marqué comme vu");
        }

        userMovieListRepository.deleteByUserAndMovieAndListType(user, movie, WATCHLIST);

        UserMovieList userMovieList = new UserMovieList(user, movie, WATCHED);

        if (watchedDate != null && !watchedDate.trim().isEmpty()) {
            userMovieList.setWatchedDate(LocalDateTime.parse(watchedDate + "T00:00:00"));
        } else {
            userMovieList.setWatchedDate(LocalDateTime.now());
        }

        if (personalRating != null && personalRating > 0) {
            userMovieList.setUserRating(personalRating);
        }

        if (comment != null && !comment.trim().isEmpty()) {
            userMovieList.setUserNotes(comment);
        }

        return userMovieListRepository.save(userMovieList);
    }

    /**
     * Retire un film d'une liste utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @param tmdbId Identifiant TMDB du film
     * @param listType Type de liste (WATCHLIST, WATCHED, ...)
     */
    @Transactional
    public void removeFromList(Long userId, Long tmdbId, ListType listType) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Movie movie = movieRepository.findByTmdbId(tmdbId)
            .orElseThrow(MovieNotFoundException::new);

        userMovieListRepository.deleteByUserAndMovieAndListType(user, movie, listType);
    }

    /**
     * Met à jour la note et le commentaire d'un film vu.
     * @param userId Identifiant de l'utilisateur
     * @param tmdbId Identifiant TMDB du film
     * @param rating Note (1-10)
     * @param notes Commentaire
     * @return UserMovieList mise à jour
     */
    @Transactional
    public UserMovieList updateUserRating(Long userId, Long tmdbId, Integer rating, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        Movie movie = movieRepository.findByTmdbId(tmdbId)
                .orElseThrow(MovieNotFoundException::new);

        UserMovieList userMovieList = userMovieListRepository.findByUserAndMovieAndListType(user, movie, WATCHED)
            .orElseThrow(() -> new UserMovieListNotFoundException("Film non trouvé dans la liste des films vus"));

        if (rating != null && (rating < 1 || rating > 10)) {
            throw new InvalidUserMovieListOperationException("La note doit être comprise entre 1 et 10");
        }

        userMovieList.setUserRating(rating);
        userMovieList.setUserNotes(notes);

        return userMovieListRepository.save(userMovieList);
    }

    private Movie getOrCreateMovie(Long tmdbId) {
        Optional<Movie> existingMovie = movieRepository.findByTmdbId(tmdbId);
        if (existingMovie.isPresent()) {
            return existingMovie.get();
        }

        Movie movieFromApi = tmdbService.getMovieDetails(tmdbId);
        if (movieFromApi == null) {
            throw new MovieNotFoundException();
        }

        return movieRepository.save(movieFromApi);
    }

    /**
     * Récupère le nombre de films dans la watchlist de l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @return Nombre de films à voir
     */
    public long getWatchlistCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMovieListRepository.countByUserAndListType(user, WATCHLIST);
    }

    /**
     * Récupère le nombre de films vus par l'utilisateur.
     * @param userId Identifiant de l'utilisateur
     * @return Nombre de films vus
     */
    public long getWatchedCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);
        return userMovieListRepository.countByUserAndListType(user, WATCHED);
    }
}
