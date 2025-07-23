package com.nobass.server.services;

import com.nobass.server.entities.User;
import com.nobass.server.enums.UserRole;
import com.nobass.server.exceptions.InvalidCredentialsException;
import com.nobass.server.exceptions.UserAlreadyExistsException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.UserRepository;
import com.nobass.server.security.JwtUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @InjectMocks
    AuthService authService;

    @Mock
    UserRepository userRepository;
    @Mock
    JwtUtil jwtUtil;
    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void register_createsNewUserWhenEmailAndUsernameAreUnique() {
        User user = new User();
        user.setEmail("unique@example.com");
        user.setUsername("uniqueUser");
        user.setPassword("password");
        user.setRole(UserRole.USER);
        user.setId(1L);

        when(userRepository.findByEmail(user.getEmail())).thenReturn(empty());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(empty());
        when(userRepository.save(user)).thenReturn(user);
        when(jwtUtil.generateToken("uniqueUser", 1L)).thenReturn("token");

        Map<String, Object> result = (Map<String, Object>) authService.register(user);

        assertEquals("token", result.get("token"));
        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals("uniqueUser", userInfo.get("username"));
        assertEquals("unique@example.com", userInfo.get("email"));
        assertEquals(1L, userInfo.get("id"));
        assertEquals("USER", userInfo.get("role"));
        verify(userRepository).save(user);
    }

    @Test
    void register_throwsExceptionWhenEmailAlreadyExists() {
        User user = new User();
        user.setEmail("existing@example.com");
        user.setUsername("uniqueUser");
        user.setPassword("password");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(user));
    }

    @Test
    void register_throwsExceptionWhenUsernameAlreadyExists() {
        User user = new User();
        user.setEmail("unique@example.com");
        user.setUsername("existingUser");
        user.setPassword("password");

        when(userRepository.findByEmail(user.getEmail())).thenReturn(empty());
        when(userRepository.findByUsername(user.getUsername())).thenReturn(of(user));

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(user));
    }

    @Test
    void login_returnsTokenWhenCredentialsAreValid() {
        User user = new User();
        user.setUsername("validUser");
        user.setId(1L);
        user.setPassword(passwordEncoder.encode("validPassword"));
        user.setRole(UserRole.USER);
        user.setEmail("valid@example.com");

        Map<String, String> credentials = Map.of("username", "validUser", "password", "validPassword");

        when(userRepository.findByUsername("validUser")).thenReturn(of(user));
        when(passwordEncoder.matches("validPassword", user.getPassword())).thenReturn(true);
        when(jwtUtil.generateToken("validUser", 1L)).thenReturn("token");

        Map<String, Object> result = (Map<String, Object>) authService.login(credentials);

        assertEquals("token", result.get("token"));
        Map<String, Object> userInfo = (Map<String, Object>) result.get("user");
        assertEquals("validUser", userInfo.get("username"));
        assertEquals("valid@example.com", userInfo.get("email"));
        assertEquals(1L, userInfo.get("id"));
        assertEquals("USER", userInfo.get("role"));
    }

    @Test
    void login_throwsExceptionWhenUserNotFound() {
        Map<String, String> credentials = Map.of("username", "nonExistentUser", "password", "password");

        when(userRepository.findByUsername("nonExistentUser")).thenReturn(empty());

        assertThrows(UserNotFoundException.class, () -> authService.login(credentials));
    }

    @Test
    void login_throwsExceptionWhenPasswordIsInvalid() {
        User user = new User();
        user.setUsername("validUser");
        user.setPassword(passwordEncoder.encode("validPassword"));

        Map<String, String> credentials = Map.of("username", "validUser", "password", "invalidPassword");

        when(userRepository.findByUsername("validUser")).thenReturn(of(user));
        when(passwordEncoder.matches("invalidPassword", user.getPassword())).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(credentials));
    }
}
