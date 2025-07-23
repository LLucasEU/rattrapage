package com.nobass.server.entities;

import com.nobass.server.enums.UserRole;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Entité représentant un utilisateur de l'application.
 * <p>
 * Contient les informations d'identification, de contact, le rôle et la liste des films associés à l'utilisateur.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    /** Identifiant interne (auto-généré) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nom d'utilisateur (unique, obligatoire) */
    @Column(unique = true, nullable = false)
    private String username;

    /** Adresse e-mail (unique, obligatoire) */
    @Column(unique = true, nullable = false)
    private String email;

    /** Mot de passe hashé (obligatoire) */
    @Column(nullable = false)
    private String password;

    /** Rôle de l'utilisateur (USER, ADMIN, etc.) */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /** Listes de films associées à l'utilisateur (watchlist, vus, etc.) */
    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    @com.fasterxml.jackson.annotation.JsonManagedReference
    private List<UserMovieList> userMovieLists;

    /**
     * Constructeur par défaut (requis par JPA).
     */
    public User() {}
}


