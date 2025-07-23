package com.nobass.server.services;

import com.nobass.server.entities.User;
import com.nobass.server.enums.UserRole;
import com.nobass.server.exceptions.UserAlreadyExistsException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des utilisateurs.
 * <p>
 * Fournit les opérations CRUD, la gestion du profil, du mot de passe et des rôles.
 * </p>
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Récupère tous les utilisateurs.
     * @return Liste des utilisateurs
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Récupère un utilisateur par son identifiant.
     * @param id Identifiant de l'utilisateur
     * @return Optional contenant l'utilisateur s'il existe
     */
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Récupère un utilisateur par son nom d'utilisateur.
     * @param username Nom d'utilisateur
     * @return L'utilisateur trouvé
     */
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
    }

    /**
     * Crée ou met à jour un utilisateur.
     * @param user Utilisateur à sauvegarder
     * @return L'utilisateur sauvegardé
     */
    public User saveOrUpdateUser(User user) {
        return userRepository.save(user);
    }

    /**
     * Supprime un utilisateur par son identifiant.
     * @param id Identifiant de l'utilisateur
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Modifie le rôle d'un utilisateur.
     * @param id Identifiant de l'utilisateur
     * @param newRole Nouveau rôle (USER, ADMIN, ...)
     * @return L'utilisateur mis à jour
     */
    public User updateUserRole(Long id, String newRole) {
        User user = userRepository.findById(id)
            .orElseThrow(UserNotFoundException::new);

        try {
            UserRole role = UserRole.valueOf(newRole.toUpperCase());
            user.setRole(role);
            return userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Rôle invalide: " + newRole);
        }
    }

    /**
     * Met à jour le profil (username, email) d'un utilisateur.
     * @param currentUsername Nom d'utilisateur actuel
     * @param newUsername Nouveau nom d'utilisateur
     * @param newEmail Nouvelle adresse e-mail
     * @return L'utilisateur mis à jour
     */
    public User updateUserProfile(String currentUsername, String newUsername, String newEmail) {
        User currentUser = userRepository.findByUsername(currentUsername)
                .orElseThrow(UserNotFoundException::new);

        if (newUsername != null && !newUsername.equals(currentUsername)) {
            Optional<User> existingUser = userRepository.findByUsername(newUsername);
            if (existingUser.isPresent()) {
                throw new UserAlreadyExistsException("Ce nom d'utilisateur est déjà utilisé");
            }
        }
        if (newEmail != null && !newEmail.equals(currentUser.getEmail())) {
            Optional<User> existingUser = userRepository.findByEmail(newEmail);
            if (existingUser.isPresent()) {
                throw new UserAlreadyExistsException("Cet email est déjà utilisé");
            }
        }
        if (newUsername != null) {
            currentUser.setUsername(newUsername);
        }
        if (newEmail != null) {
            currentUser.setEmail(newEmail);
        }
        return userRepository.save(currentUser);
    }

    /**
     * Change le mot de passe d'un utilisateur.
     * @param username Nom d'utilisateur
     * @param currentPassword Ancien mot de passe
     * @param newPassword Nouveau mot de passe
     */
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(UserNotFoundException::new);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Mot de passe actuel incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
