package com.nobass.server.security;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

import static java.lang.System.currentTimeMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    @Mock
    private Claims claims;

    private String validToken;
    private final String username = "testUser";

    @BeforeEach
    void setUp() {
        openMocks(this);
        Long userId = 1L;
        validToken = jwtUtil.generateToken(username, userId);
    }

    @Test
    void generateToken_createsValidToken() {
        assertNotNull(validToken);
        assertFalse(validToken.isEmpty());
    }

    @Test
    void extractUsername_returnsCorrectUsername() {
        String extractedUsername = jwtUtil.extractUsername(validToken);
        assertEquals(username, extractedUsername);
    }

    @Test
    void validateToken_returnsTrueForValidToken() {
        assertTrue(jwtUtil.validateToken(validToken, username));
    }

    @Test
    void validateToken_returnsFalseForInvalidUsername() {
        assertFalse(jwtUtil.validateToken(validToken, "wrongUser"));
    }

    @Test
    void extractClaim_usesClaimsResolver() {
        Function<Claims, String> claimsResolver = Claims::getSubject;
        when(claims.getSubject()).thenReturn(username);

        String extractedClaim = claimsResolver.apply(claims);

        assertEquals(username, extractedClaim);
    }

    @Test
    void isTokenExpired_returnsTrueForExpiredToken() {
        Claims mockClaims = mock(Claims.class);
        when(mockClaims.getExpiration()).thenReturn(new Date(currentTimeMillis() - 10000));

        Function<Claims, Date> expirationResolver = Claims::getExpiration;
        assertTrue(expirationResolver.apply(mockClaims).before(new Date()));
    }

    @Test
    void getSigningKey_returnsValidKey() {
        SecretKey key = jwtUtil.getSigningKey();

        assertNotNull(key);
        assertEquals(32, key.getEncoded().length);
    }
}
