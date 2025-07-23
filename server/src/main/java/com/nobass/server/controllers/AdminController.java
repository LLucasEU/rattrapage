package com.nobass.server.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour les opérations d'administration.
 * <p>
 * Toutes les routes de ce contrôleur sont réservées aux utilisateurs ayant le rôle ADMIN.
 * </p>
 */
@RestController
@RequestMapping("/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    /**
     * Endpoint du dashboard d'administration.
     * <p>
     * Accessible uniquement aux administrateurs.
     * </p>
     * @return Un message de bienvenue pour le dashboard admin.
     */
    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> getAdminDashboard() {
        return ResponseEntity.ok("Bienvenue sur le Dashboard Admin !");
    }
}
