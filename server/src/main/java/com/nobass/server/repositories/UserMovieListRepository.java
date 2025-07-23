package com.nobass.server.repositories;

import com.nobass.server.entities.User;
import com.nobass.server.entities.UserMovieList;
import com.nobass.server.enums.ListType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité UserMovieList.
 * <p>
 * Permet d'effectuer des opérations CRUD et des requêtes personnalisées sur les listes de films utilisateur.
 * </p>
 */
@Repository
public interface UserMovieListRepository extends JpaRepository<UserMovieList, Long> {

    /**
     * Récupère la liste des UserMovieList pour un utilisateur et un type de liste, triée par date d'ajout décroissante.
     * @param user L'utilisateur concerné
     * @param listType Le type de liste (WATCHLIST, WATCHED, etc.)
     * @return Liste des UserMovieList correspondantes
     */
    @Query("SELECT uml FROM UserMovieList uml WHERE uml.user = :user AND uml.listType = :listType ORDER BY uml.addedDate DESC")
    List<UserMovieList> findByUserAndListTypeOrderByAddedDateDesc(@Param("user") User user, @Param("listType") ListType listType);

    /**
     * Recherche une UserMovieList par utilisateur, film et type de liste.
     * @param user L'utilisateur
     * @param movie Le film
     * @param listType Le type de liste
     * @return Un Optional contenant la UserMovieList si elle existe
     */
    Optional<UserMovieList> findByUserAndMovieAndListType(User user, com.nobass.server.entities.Movie movie, ListType listType);

    /**
     * Vérifie si une UserMovieList existe pour un utilisateur, un film et un type de liste.
     * @param user L'utilisateur
     * @param movie Le film
     * @param listType Le type de liste
     * @return true si la UserMovieList existe, false sinon
     */
    boolean existsByUserAndMovieAndListType(User user, com.nobass.server.entities.Movie movie, ListType listType);

    /**
     * Supprime une UserMovieList pour un utilisateur, un film et un type de liste.
     * @param user L'utilisateur
     * @param movie Le film
     * @param listType Le type de liste
     */
    void deleteByUserAndMovieAndListType(User user, com.nobass.server.entities.Movie movie, ListType listType);

    /**
     * Compte le nombre de UserMovieList pour un utilisateur et un type de liste.
     * @param user L'utilisateur
     * @param listType Le type de liste
     * @return Le nombre de UserMovieList correspondantes
     */
    @Query("SELECT COUNT(uml) FROM UserMovieList uml WHERE uml.user = :user AND uml.listType = :listType")
    long countByUserAndListType(@Param("user") User user, @Param("listType") ListType listType);
}
