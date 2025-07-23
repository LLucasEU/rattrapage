package com.nobass.server.controllers;

import com.nobass.server.entities.User;
import com.nobass.server.services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Contrôleur REST pour l'authentification des utilisateurs (inscription et connexion).
 * <p>
 * Fournit les endpoints pour s'inscrire et se connecter, et retourne un JWT en cas de succès.
 * </p>
 */
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "http://localhost:4200")
public class AuthController {

    private final AuthService authService;

    /**
     * Constructeur avec injection du service d'authentification.
     * @param authService Service métier pour la gestion de l'authentification
     */
    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint d'inscription d'un nouvel utilisateur.
     * <p>
     * Reçoit un objet User en JSON, crée le compte si les données sont valides et non déjà utilisées.
     * Retourne un JWT et les infos utilisateur en cas de succès.
     * </p>
     * @param user L'utilisateur à enregistrer
     * @return 200 OK avec le token et l'utilisateur, ou 400 en cas d'erreur (données invalides, doublon...)
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        try {
            Map<String, Object> response = authService.register(user);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Endpoint de connexion utilisateur.
     * <p>
     * Reçoit un JSON {username, password}, vérifie les identifiants et retourne un JWT si la connexion est valide.
     * </p>
     * @param credentials Mappe contenant "username" et "password"
     * @return 200 OK avec le token et l'utilisateur, ou 400 en cas d'erreur (identifiants invalides...)
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        try {
            Map<String, Object> response = authService.login(credentials);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
