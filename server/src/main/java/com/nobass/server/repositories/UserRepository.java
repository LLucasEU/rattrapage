package com.nobass.server.repositories;

import com.nobass.server.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository Spring Data JPA pour l'entité User.
 * <p>
 * Permet d'effectuer des opérations CRUD et des requêtes personnalisées sur les utilisateurs.
 * </p>
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Recherche un utilisateur par son nom d'utilisateur.
     * @param username Nom d'utilisateur
     * @return Un Optional contenant l'utilisateur s'il existe
     */
    Optional<User> findByUsername(String username);
    /**
     * Recherche un utilisateur par son adresse e-mail.
     * @param email Adresse e-mail
     * @return Un Optional contenant l'utilisateur s'il existe
     */
    Optional<User> findByEmail(String email);
}
