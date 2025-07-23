package com.nobass.server.services;

import com.nobass.server.entities.User;
import com.nobass.server.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static com.nobass.server.enums.UserRole.USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {
    @InjectMocks
    CustomUserDetailsService customUserDetailsService;

    @Mock
    UserRepository userRepository;

    @Test
    void loadUserByUsername_returnsUserDetailsWhenUserExists() {
        User user = new User();
        user.setUsername("existingUser");
        user.setPassword("password");
        user.setRole(USER);

        when(userRepository.findByUsername("existingUser")).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserByUsername("existingUser");

        assertEquals("existingUser", userDetails.getUsername());
        assertEquals("password", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserByUsername_throwsExceptionWhenUserNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserByUsername("nonExistentUser"));
    }

    @Test
    void loadUserById_retourneUserDetails_QuandUserExiste() {
        User user = new User();
        user.setId(42L);
        user.setUsername("bob");
        user.setPassword("pass");
        user.setRole(USER);

        when(userRepository.findById(42L)).thenReturn(Optional.of(user));

        UserDetails userDetails = customUserDetailsService.loadUserById(42L);
        assertEquals("bob", userDetails.getUsername());
        assertEquals("pass", userDetails.getPassword());
        assertTrue(userDetails.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_USER")));
    }

    @Test
    void loadUserById_lanceException_QuandUserNonTrouvé() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () -> customUserDetailsService.loadUserById(99L));
    }
}
