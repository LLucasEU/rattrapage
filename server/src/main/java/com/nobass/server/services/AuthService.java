package com.nobass.server.services;

import com.nobass.server.entities.User;
import com.nobass.server.enums.UserRole;
import com.nobass.server.exceptions.InvalidCredentialsException;
import com.nobass.server.exceptions.NotAuthenticatedException;
import com.nobass.server.exceptions.UserAlreadyExistsException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.UserRepository;
import com.nobass.server.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Service métier pour la gestion de l'authentification (inscription, connexion, gestion du JWT).
 * <p>
 * Fournit les opérations de création de compte, login, génération de token et récupération de l'utilisateur courant.
 * </p>
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Inscrit un nouvel utilisateur (vérifie unicité email/username, hash le mot de passe, génère un JWT).
     * @param user Utilisateur à enregistrer
     * @return Map contenant le token et les infos utilisateur
     */
    public Map<String, Object> register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new UserAlreadyExistsException("Email déjà utilisé.");
        }
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new UserAlreadyExistsException("Nom d'utilisateur déjà utilisé.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(UserRole.USER);
        }
        User savedUser = userRepository.save(user);
        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(savedUser.getUsername(), savedUser.getId()));
        response.put("user", createUserInfo(savedUser));
        return response;
    }

    /**
     * Authentifie un utilisateur à partir de ses identifiants.
     * @param credentials Mappe contenant "username" et "password"
     * @return Map contenant le token et les infos utilisateur
     */
    public Map<String, Object> login(Map<String, String> credentials) {
        User user = userRepository.findByUsername(credentials.get("username"))
                .orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(credentials.get("password"), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        Map<String, Object> response = new HashMap<>();
        response.put("token", jwtUtil.generateToken(user.getUsername(), user.getId()));
        response.put("user", createUserInfo(user));

        return response;
    }

    /**
     * Génère un JWT pour un utilisateur donné.
     * @param user Utilisateur
     * @return Token JWT
     */
    public String generateToken(User user) {
        return jwtUtil.generateToken(user.getUsername(), user.getId());
    }

    private Map<String, Object> createUserInfo(User user) {
        return Map.of(
            "id", user.getId(),
            "username", user.getUsername(),
            "email", user.getEmail(),
            "role", user.getRole().name()
        );
    }

    /**
     * Récupère l'identifiant de l'utilisateur actuellement authentifié.
     * @return L'id de l'utilisateur courant
     */
    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new NotAuthenticatedException();
        }

        String username = authentication.getName();
        Object credentials = authentication.getCredentials();

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            return userOpt.get().getId();
        }

        Long userId = null;
        if (credentials instanceof Long) {
            userId = (Long) credentials;
        }
        if (userId != null) {
            return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé (par ID)"))
                .getId();
        }

        throw new UserNotFoundException();
    }
}
