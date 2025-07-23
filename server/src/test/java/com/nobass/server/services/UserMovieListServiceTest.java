package com.nobass.server.services;

import com.nobass.server.entities.Movie;
import com.nobass.server.entities.User;
import com.nobass.server.entities.UserMovieList;
import com.nobass.server.exceptions.InvalidUserMovieListOperationException;
import com.nobass.server.exceptions.MovieNotFoundException;
import com.nobass.server.exceptions.UserMovieListNotFoundException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.MovieRepository;
import com.nobass.server.repositories.UserMovieListRepository;
import com.nobass.server.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static com.nobass.server.enums.ListType.WATCHED;
import static com.nobass.server.enums.ListType.WATCHLIST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserMovieListServiceTest {
    @InjectMocks
    UserMovieListService service;

    @Mock
    UserMovieListRepository userMovieListRepository;
    @Mock
    MovieRepository movieRepository;
    @Mock
    UserRepository userRepository;
    @Mock
    TmdbService tmdbService;

    @Test
    void getUserWatchlist_returnsList_whenUserExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        List<UserMovieList> expected = List.of(new UserMovieList(), new UserMovieList());
        when(userMovieListRepository.findByUserAndListTypeOrderByAddedDateDesc(user, WATCHLIST)).thenReturn(expected);

        List<UserMovieList> result = service.getUserWatchlist(1L);

        assertEquals(2, result.size());
    }

    @Test
    void getUserWatchlist_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUserWatchlist(1L));
    }

    @Test
    void getUserWatchedMovies_returnsList_whenUserExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        List<UserMovieList> expected = List.of(new UserMovieList());
        when(userMovieListRepository.findByUserAndListTypeOrderByAddedDateDesc(user, WATCHED)).thenReturn(expected);

        List<UserMovieList> result = service.getUserWatchedMovies(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getUserWatchedMovies_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getUserWatchedMovies(1L));
    }

    @Test
    void addToWatchlist_addsMovie_whenNotPresent() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHLIST)).thenReturn(false);
        when(userMovieListRepository.save(any())).thenReturn(new UserMovieList());

        UserMovieList result = service.addToWatchlist(1L, 10L);

        assertNotNull(result);
    }

    @Test
    void addToWatchlist_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.addToWatchlist(1L, 10L));
    }

    @Test
    void addToWatchlist_throwsException_whenMovieAlreadyInWatchlist() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHLIST)).thenReturn(true);

        assertThrows(InvalidUserMovieListOperationException.class, () -> service.addToWatchlist(1L, 10L));
    }

    @Test
    void markAsWatched_addsMovie_whenNotPresent() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHED)).thenReturn(false);
        when(userMovieListRepository.save(any())).thenReturn(new UserMovieList());

        UserMovieList result = service.markAsWatched(1L, 10L, null, 8, "Great");

        assertNotNull(result);
    }

    @Test
    void markAsWatched_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.markAsWatched(1L, 10L, null, 8, "Great"));
    }

    @Test
    void markAsWatched_throwsException_whenMovieAlreadyWatched() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.existsByUserAndMovieAndListType(user, movie, WATCHED)).thenReturn(true);

        assertThrows(InvalidUserMovieListOperationException.class, () -> service.markAsWatched(1L, 10L, null, 8, "Great"));
    }

    @Test
    void removeFromList_removesMovie_whenPresent() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));

        service.removeFromList(1L, 10L, WATCHLIST);

        verify(userMovieListRepository).deleteByUserAndMovieAndListType(user, movie, WATCHLIST);
    }

    @Test
    void removeFromList_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.removeFromList(1L, 10L, WATCHLIST));
    }

    @Test
    void removeFromList_throwsException_whenMovieNotFound() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> service.removeFromList(1L, 10L, WATCHLIST));
    }

    @Test
    void updateUserRating_updatesRating_whenValid() {
        User user = new User();
        Movie movie = new Movie();
        UserMovieList uml = new UserMovieList();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.findByUserAndMovieAndListType(user, movie, WATCHED)).thenReturn(Optional.of(uml));
        when(userMovieListRepository.save(uml)).thenReturn(uml);

        UserMovieList result = service.updateUserRating(1L, 10L, 7, "Nice");

        assertNotNull(result);
    }

    @Test
    void updateUserRating_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.updateUserRating(1L, 10L, 7, "Nice"));
    }

    @Test
    void updateUserRating_throwsException_whenMovieNotFound() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.empty());

        assertThrows(MovieNotFoundException.class, () -> service.updateUserRating(1L, 10L, 7, "Nice"));
    }

    @Test
    void updateUserRating_throwsException_whenMovieNotInWatchedList() {
        User user = new User();
        Movie movie = new Movie();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.findByUserAndMovieAndListType(user, movie, WATCHED)).thenReturn(Optional.empty());

        assertThrows(UserMovieListNotFoundException.class, () -> service.updateUserRating(1L, 10L, 7, "Nice"));
    }

    @Test
    void updateUserRating_throwsException_whenRatingInvalid() {
        User user = new User();
        Movie movie = new Movie();
        UserMovieList uml = new UserMovieList();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));
        when(userMovieListRepository.findByUserAndMovieAndListType(user, movie, WATCHED)).thenReturn(Optional.of(uml));

        assertThrows(InvalidUserMovieListOperationException.class, () -> service.updateUserRating(1L, 10L, 0, "Bad"));
        assertThrows(InvalidUserMovieListOperationException.class, () -> service.updateUserRating(1L, 10L, 11, "Bad"));
    }

    @Test
    void getOrCreateMovie_returnsExistingMovie_whenPresent() throws Exception {
        Movie movie = new Movie();
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.of(movie));

        var m = UserMovieListService.class.getDeclaredMethod("getOrCreateMovie", Long.class);
        m.setAccessible(true);
        Movie result = (Movie) m.invoke(service, 10L);

        assertNotNull(result);
    }

    @Test
    void getOrCreateMovie_createsMovie_whenNotPresentAndApiReturnsMovie() throws Exception {
        when(movieRepository.findByTmdbId(10L)).thenReturn(Optional.empty());
        Movie movieFromApi = new Movie();
        when(tmdbService.getMovieDetails(10L)).thenReturn(movieFromApi);
        when(movieRepository.save(movieFromApi)).thenReturn(movieFromApi);

        var m = UserMovieListService.class.getDeclaredMethod("getOrCreateMovie", Long.class);
        m.setAccessible(true);
        Movie result = (Movie) m.invoke(service, 10L);

        assertNotNull(result);
    }

    @Test
    void getWatchlistCount_returnsCount_whenUserExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMovieListRepository.countByUserAndListType(user, WATCHLIST)).thenReturn(5L);

        long count = service.getWatchlistCount(1L);

        assertEquals(5L, count);
    }

    @Test
    void getWatchlistCount_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getWatchlistCount(1L));
    }

    @Test
    void getWatchedCount_returnsCount_whenUserExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMovieListRepository.countByUserAndListType(user, WATCHED)).thenReturn(3L);

        long count = service.getWatchedCount(1L);

        assertEquals(3L, count);
    }

    @Test
    void getWatchedCount_throwsException_whenUserNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> service.getWatchedCount(1L));
    }
}
