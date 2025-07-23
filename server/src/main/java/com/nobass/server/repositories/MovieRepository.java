package com.nobass.server.repositories;

import com.nobass.server.entities.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité Movie.
 * <p>
 * Permet d'effectuer des opérations CRUD et des requêtes personnalisées sur les films.
 * </p>
 */
@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    /**
     * Recherche un film par son identifiant TMDB.
     * @param tmdbId Identifiant TMDB du film
     * @return Un Optional contenant le film s'il existe
     */
    Optional<Movie> findByTmdbId(Long tmdbId);
}
