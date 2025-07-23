package com.nobass.server.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.nobass.server.enums.ListType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entité représentant l'association entre un utilisateur et un film dans une liste (à voir, vus, etc.).
 * <p>
 * Permet de stocker les informations personnalisées de l'utilisateur sur un film (note, commentaire, dates, etc.).
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "user_movie_lists")
public class UserMovieList {

    /** Identifiant interne (auto-généré) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Utilisateur propriétaire de la liste */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonBackReference
    private User user;

    /** Film associé à la liste */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id", nullable = false)
    private Movie movie;

    /** Type de liste (WATCHLIST, WATCHED, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false)
    private ListType listType;

    /** Date d'ajout du film à la liste */
    @Column(name = "added_date", nullable = false)
    private LocalDateTime addedDate;

    /** Date à laquelle le film a été vu (optionnelle) */
    @Column(name = "watched_date")
    private LocalDateTime watchedDate;

    /** Note personnelle de l'utilisateur pour ce film (optionnelle) */
    @Column(name = "user_rating")
    private Integer userRating;

    /** Commentaire personnel de l'utilisateur (optionnel, max 1000 caractères) */
    @Column(name = "user_notes", length = 1000)
    private String userNotes;

    /**
     * Constructeur par défaut (requis par JPA).
     */
    public UserMovieList() {}

    /**
     * Constructeur principal pour créer une association utilisateur/film/liste.
     * Initialise la date d'ajout à la date courante.
     * @param user Utilisateur
     * @param movie Film
     * @param listType Type de liste (WATCHLIST, WATCHED, etc.)
     */
    public UserMovieList(User user, Movie movie, ListType listType) {
        this.user = user;
        this.movie = movie;
        this.listType = listType;
        this.addedDate = LocalDateTime.now();
    }
}
