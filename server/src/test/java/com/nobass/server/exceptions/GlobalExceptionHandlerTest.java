package com.nobass.server.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {
    @InjectMocks
    GlobalExceptionHandler globalExceptionHandler;

    @Test
    void handleUserNotFoundException_returnsNotFoundStatus() {
        UserNotFoundException ex = new UserNotFoundException();

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleUserNotFoundException(ex);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Utilisateur non trouvé", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    void handleInvalidCredentialsException_returnsUnauthorizedStatus() {
        InvalidCredentialsException ex = new InvalidCredentialsException("Invalid credentials");

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleInvalidCredentialsException(ex);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    void handleUserAlreadyExistsException_returnsConflictStatus() {
        UserAlreadyExistsException ex = new UserAlreadyExistsException("User already exists");

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleUserAlreadyExistsException(ex);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("User already exists", Objects.requireNonNull(response.getBody()).get("error"));
    }

    @Test
    void handleGenericException_returnsInternalServerErrorStatus() {
        Exception ex = new Exception("Unexpected error");

        ResponseEntity<Map<String, String>> response = globalExceptionHandler.handleGenericException(ex);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An unexpected error occurred: Unexpected error", Objects.requireNonNull(response.getBody()).get("error"));
    }
}
