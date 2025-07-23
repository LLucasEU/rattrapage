package com.nobass.server.controllers;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Contrôleur REST pour la racine de l'application.
 * <p>
 * Fournit un endpoint public pour vérifier que l'API est accessible.
 * </p>
 */
@RestController
@CrossOrigin(origins = "*")
public class HomeController {
    /**
     * Endpoint racine de l'API.
     * <p>
     * Retourne un message de bienvenue.
     * </p>
     * @return Message de bienvenue (String)
     */
    @GetMapping("/")
    public String home() {
        return "Welcome to Rattrapage!";
    }
}
