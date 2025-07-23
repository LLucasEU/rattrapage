package com.nobass.server.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

/**
 * Entité représentant un film (issu de TMDB ou ajouté par l'utilisateur).
 * <p>
 * Contient les informations principales d'un film : titre, synopsis, date de sortie, note, genres, etc.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "movies")
public class Movie {

    /** Identifiant interne (auto-généré) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Identifiant unique du film sur TMDB */
    @Column(name = "tmdb_id", unique = true, nullable = false)
    private Long tmdbId;

    /** Titre du film */
    @Column(nullable = false)
    private String title;

    /** Synopsis du film */
    @Column(length = 1000)
    private String overview;

    /** Chemin de l'affiche (poster) */
    @Column(name = "poster_path")
    private String posterPath;

    /** Date de sortie du film */
    @Column(name = "release_date")
    private LocalDate releaseDate;

    /** Note moyenne (TMDB) */
    @Column(name = "vote_average")
    private Double voteAverage;

    /** Nombre de votes (TMDB) */
    @Column(name = "vote_count")
    private Integer voteCount;

    /** Chemin de l'image de fond (backdrop) */
    @Column(name = "backdrop_path")
    private String backdropPath;

    /** Langue originale du film */
    @Column(name = "original_language")
    private String originalLanguage;

    /** Popularité (score TMDB) */
    @Column(name = "popularity")
    private Double popularity;

    /** Genres du film (liste de chaînes) */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"))
    @Column(name = "genre")
    private List<String> genres;

    /**
     * Constructeur par défaut (requis par JPA).
     */
    public Movie() {}

    /**
     * Constructeur principal pour créer un film avec toutes ses propriétés (hors id et genres).
     * @param tmdbId Identifiant TMDB
     * @param title Titre
     * @param overview Synopsis
     * @param posterPath Chemin de l'affiche
     * @param releaseDate Date de sortie
     * @param voteAverage Note moyenne
     * @param voteCount Nombre de votes
     * @param backdropPath Chemin de l'image de fond
     * @param originalLanguage Langue originale
     * @param popularity Score de popularité
     */
    public Movie(Long tmdbId, String title, String overview, String posterPath,
                 LocalDate releaseDate, Double voteAverage, Integer voteCount,
                 String backdropPath, String originalLanguage, Double popularity) {
        this.tmdbId = tmdbId;
        this.title = title;
        this.overview = overview;
        this.posterPath = posterPath;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.voteCount = voteCount;
        this.backdropPath = backdropPath;
        this.originalLanguage = originalLanguage;
        this.popularity = popularity;
    }
}
