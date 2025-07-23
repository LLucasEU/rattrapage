package com.nobass.server.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * Utilitaire pour la gestion des tokens JWT (génération, extraction, validation).
 * <p>
 * Utilisé pour l'authentification stateless dans l'application.
 * </p>
 */
@Component
public class JwtUtil {

    private static final String SECRET_KEY = "abzCy6l8W/hWfX/2ofMvTC++mgQYuMPHKxDLm7ncHVk=";
    private static final long EXPIRATION_TIME = 86400000;

    protected SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET_KEY);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Génère un token JWT pour un utilisateur donné.
     * @param username Nom d'utilisateur
     * @param userId Identifiant utilisateur
     * @return Token JWT signé
     */
    public String generateToken(String username, Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("id", userId);

        return Jwts.builder()
                .claims(claims)
                .subject(username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Extrait le nom d'utilisateur du token JWT.
     * @param token Token JWT
     * @return Nom d'utilisateur
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrait l'identifiant utilisateur du token JWT.
     * @param token Token JWT
     * @return Identifiant utilisateur
     */
    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("id", Long.class));
    }

    /**
     * Valide le token JWT en vérifiant le nom d'utilisateur et l'expiration.
     * @param token Token JWT
     * @param username Nom d'utilisateur attendu
     * @return true si le token est valide
     */
    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && isTokenExpired(token);
    }

    /**
     * Valide le token JWT en vérifiant l'identifiant utilisateur et l'expiration.
     * @param token Token JWT
     * @param userId Identifiant utilisateur attendu
     * @return true si le token est valide
     */
    public boolean validateTokenById(String token, Long userId) {
        return userId.equals(extractUserId(token)) && isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return !extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claimsResolver.apply(claims);
    }
}
