package com.nobass.server.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

@ExtendWith(SpringExtension.class)
@SpringBootTest
class SecurityConfigTest {

    @Autowired
    private SecurityConfig securityConfig;

    @Mock
    private AuthenticationConfiguration authenticationConfiguration;

    @Test
    void securityFilterChain_addsJwtAuthenticationFilter() throws Exception {
        SecurityFilterChain filterChain = securityConfig.securityFilterChain(mock(HttpSecurity.class));

        assertNotNull(filterChain);
    }

    @Test
    void authenticationManager_returnsAuthenticationManager() throws Exception {
        AuthenticationManager result = securityConfig.authenticationManager(authenticationConfiguration);

        assertInstanceOf(AuthenticationManager.class, result);
    }

    @Test
    void passwordEncoder_returnsBCryptPasswordEncoder() {
        PasswordEncoder passwordEncoder = securityConfig.passwordEncoder();
        assertInstanceOf(PasswordEncoder.class, passwordEncoder);
    }
}
