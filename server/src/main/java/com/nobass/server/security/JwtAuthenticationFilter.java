package com.nobass.server.security;

import com.nobass.server.services.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtre Spring Security pour l'authentification JWT.
 * <p>
 * Intercepte chaque requête HTTP, extrait et valide le token JWT, et pose l'authentification dans le contexte si le token est valide.
 * </p>
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    /**
     * Intercepte chaque requête HTTP, extrait et valide le JWT, pose l'authentification si valide.
     * @param request Requête HTTP
     * @param response Réponse HTTP
     * @param filterChain Chaîne de filtres
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String token = extractToken(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String username = jwtUtil.extractUsername(token);
            Long userId = jwtUtil.extractUserId(token);

            if (username != null && shouldSetAuthentication()) {
                UserDetails userDetails = loadUserDetails(username, userId);
                if (userDetails != null && validateToken(token, userDetails, userId)) {
                    setAuthentication(userDetails, userId, request);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring(7);
    }

    private boolean shouldSetAuthentication() {
        var currentAuth = SecurityContextHolder.getContext().getAuthentication();
        return currentAuth == null ||
                "anonymousUser".equals(currentAuth.getName()) ||
                currentAuth.getAuthorities().stream().anyMatch(auth -> auth.getAuthority().equals("ROLE_ANONYMOUS"));
    }

    private UserDetails loadUserDetails(String username, Long userId) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            try {
                return userDetailsService.loadUserById(userId);
            } catch (Exception e2) {
                return null;
            }
        }
    }

    private boolean validateToken(String token, UserDetails userDetails, Long userId) {
        return jwtUtil.validateToken(token, userDetails.getUsername()) ||
                jwtUtil.validateTokenById(token, userId);
    }

    private void setAuthentication(UserDetails userDetails, Long userId, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, userId, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
