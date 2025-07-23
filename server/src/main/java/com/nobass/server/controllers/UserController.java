package com.nobass.server.controllers;

import com.nobass.server.entities.User;
import com.nobass.server.exceptions.AdminAccessDeniedException;
import com.nobass.server.exceptions.NotAuthenticatedException;
import com.nobass.server.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Contrôleur REST pour la gestion des utilisateurs.
 * <p>
 * Permet la consultation, la modification, la suppression, la gestion du profil et du mot de passe, ainsi que l'administration des rôles utilisateurs.
 * </p>
 */
@RestController
@RequestMapping("/api/v1/users")
@CrossOrigin(origins = "http://localhost:4200")
public class UserController {

    private final UserService userService;
    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    private String getAuthenticatedUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NotAuthenticatedException();
        }
        if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User) {
            org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
            return userDetails.getUsername();
        } else {
            return authentication.getName();
        }
    }

    private void checkAdminOrThrow() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new NotAuthenticatedException();
        }
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            throw new AdminAccessDeniedException();
        }
    }

    /**
     * Récupère les informations de l'utilisateur actuellement authentifié.
     * @return Les informations de l'utilisateur (id, username, email, rôle)
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            String username = getAuthenticatedUsername();
            User user = userService.getUserByUsername(username);
            Map<String, Object> userInfo = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            );
            return ResponseEntity.ok(userInfo);
        } catch (RuntimeException e) {
            if ("Non authentifié".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne"));
        }
    }

    /**
     * Met à jour le profil (username, email) de l'utilisateur connecté.
     * @param request Map contenant les nouveaux username et/ou email
     * @return Les informations mises à jour de l'utilisateur
     */
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String email = request.get("email");
            String currentUsername = getAuthenticatedUsername();
            User updatedUser = userService.updateUserProfile(currentUsername, username, email);
            Map<String, Object> response = Map.of(
                "id", updatedUser.getId(),
                "username", updatedUser.getUsername(),
                "email", updatedUser.getEmail(),
                "role", updatedUser.getRole().name()
            );
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            if ("Non authentifié".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne"));
        }
    }

    /**
     * Change le mot de passe de l'utilisateur connecté.
     * @param request Map contenant l'ancien et le nouveau mot de passe
     * @return Message de succès ou d'erreur
     */
    @PutMapping("/password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> request) {
        try {
            String currentPassword = request.get("currentPassword");
            String newPassword = request.get("newPassword");
            String username = getAuthenticatedUsername();
            userService.changePassword(username, currentPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Mot de passe modifié avec succès"));
        } catch (RuntimeException e) {
            if ("Non authentifié".equals(e.getMessage())) {
                return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
            }
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne"));
        }
    }

    /**
     * Récupère la liste de tous les utilisateurs (admin uniquement).
     * @return Liste des utilisateurs (id, username, email, rôle)
     */
    @GetMapping
    public ResponseEntity<List<Map<String, ?>>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        List<Map<String, ?>> userDtos = users.stream()
            .map(user -> Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "role", user.getRole().name()
            ))
            .collect(Collectors.toList());
        return ResponseEntity.ok(userDtos);
    }

    /**
     * Récupère un utilisateur par son identifiant.
     * @param id Identifiant de l'utilisateur
     * @return L'utilisateur trouvé ou 404 si absent
     */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        return user.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /**
     * Crée ou met à jour un utilisateur (admin uniquement).
     * @param user Utilisateur à sauvegarder
     * @return L'utilisateur sauvegardé
     */
    @PostMapping
    public ResponseEntity<User> saveOrUpdateUser(@RequestBody User user) {
        User savedUser = userService.saveOrUpdateUser(user);
        return ResponseEntity.created(URI.create("/api/v1/users/" + savedUser.getId())).body(savedUser);
    }

    /**
     * Modifie le rôle d'un utilisateur (admin uniquement).
     * @param id Identifiant de l'utilisateur
     * @param request Map contenant le nouveau rôle ("role")
     * @return Message de succès et informations de l'utilisateur mis à jour
     */
    @PutMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            checkAdminOrThrow();
            String newRole = request.get("role");
            User updatedUser = userService.updateUserRole(id, newRole);
            return ResponseEntity.ok(Map.of(
                "message", "Rôle utilisateur mis à jour avec succès",
                "user", Map.of(
                    "id", updatedUser.getId(),
                    "username", updatedUser.getUsername(),
                    "email", updatedUser.getEmail(),
                    "role", updatedUser.getRole().name()
                )
            ));
        } catch (NotAuthenticatedException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne"));
        }
    }

    /**
     * Supprime un utilisateur (admin uniquement, impossible de se supprimer soi-même).
     * @param id Identifiant de l'utilisateur à supprimer
     * @return Message de succès ou d'erreur
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            checkAdminOrThrow();
            String currentUsername = getAuthenticatedUsername();
            User userToDelete = userService.getUserById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));
            if (currentUsername.equals(userToDelete.getUsername())) {
                return ResponseEntity.badRequest().body(Map.of("error", "Vous ne pouvez pas vous supprimer vous-même"));
            }
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "Utilisateur supprimé avec succès"));
        } catch (NotAuthenticatedException e) {
            return ResponseEntity.status(401).body(Map.of("error", e.getMessage()));
        } catch (AdminAccessDeniedException e) {
            return ResponseEntity.status(403).body(Map.of("error", e.getMessage()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(500).body(Map.of("error", "Erreur interne"));
        }
    }
}
