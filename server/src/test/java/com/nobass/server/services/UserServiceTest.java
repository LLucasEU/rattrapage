package com.nobass.server.services;

import com.nobass.server.entities.User;
import com.nobass.server.enums.UserRole;
import com.nobass.server.exceptions.UserAlreadyExistsException;
import com.nobass.server.exceptions.UserNotFoundException;
import com.nobass.server.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    UserService userService;

    @Mock
    UserRepository userRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void getAllUsers_returnsList_whenRepositoryCalled() {
        List<User> users = List.of(new User(), new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.getAllUsers();


        assertEquals(2, result.size());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_returnsUser_whenIdExists() {
        User user = new User();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isPresent());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserById_returnsEmpty_whenIdDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<User> result = userService.getUserById(1L);

        assertTrue(result.isEmpty());
        verify(userRepository).findById(1L);
    }

    @Test
    void getUserByUsername_returnsUser_whenUsernameExists() {
        User user = new User();
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));

        User result = userService.getUserByUsername("bob");

        assertNotNull(result);
        verify(userRepository).findByUsername("bob");
    }

    @Test
    void getUserByUsername_throwsException_whenUsernameDoesNotExist() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserByUsername("bob"));
        verify(userRepository).findByUsername("bob");
    }

    @Test
    void saveOrUpdateUser_savesUser_whenCalled() {
        User user = new User();
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.saveOrUpdateUser(user);

        assertEquals(user, result);
        verify(userRepository).save(user);
    }

    @Test
    void deleteUser_deletesUser_whenIdValid() {
        userService.deleteUser(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void updateUserRole_changesRole_whenRoleValid() {
        User user = new User();
        user.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUserRole(1L, "ADMIN");

        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userRepository).findById(1L);
        verify(userRepository).save(user);
    }

    @Test
    void updateUserRole_throwsException_whenRoleInvalid() {
        User user = new User();
        user.setRole(UserRole.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(RuntimeException.class, () -> userService.updateUserRole(1L, "FAKE"));
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUserRole_throwsException_whenUserDoesNotExist() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> userService.updateUserRole(1L, "ADMIN"));
        verify(userRepository).findById(1L);
    }

    @Test
    void updateUserProfile_changesUsernameAndEmail_whenNewOnesNotUsed() {
        User user = new User();
        user.setUsername("bob");
        user.setEmail("bob@a.com");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.empty());
        when(userRepository.findByEmail("alice@a.com")).thenReturn(Optional.empty());
        when(userRepository.save(user)).thenReturn(user);

        User result = userService.updateUserProfile("bob", "alice", "alice@a.com");

        assertEquals("alice", result.getUsername());
        assertEquals("alice@a.com", result.getEmail());
        verify(userRepository).findByUsername("bob");
        verify(userRepository).findByUsername("alice");
        verify(userRepository).findByEmail("alice@a.com");
        verify(userRepository).save(user);
    }

    @Test
    void updateUserProfile_throwsException_whenUsernameTaken() {
        User user = new User();
        user.setUsername("bob");
        user.setEmail("bob@a.com");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUserProfile("bob", "alice", null));
        verify(userRepository).findByUsername("bob");
        verify(userRepository).findByUsername("alice");
    }

    @Test
    void updateUserProfile_throwsException_whenEmailTaken() {
        User user = new User();
        user.setUsername("bob");
        user.setEmail("bob@a.com");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("alice@a.com")).thenReturn(Optional.of(new User()));

        assertThrows(UserAlreadyExistsException.class, () -> userService.updateUserProfile("bob", null, "alice@a.com"));
        verify(userRepository).findByUsername("bob");
        verify(userRepository).findByEmail("alice@a.com");
    }

    @Test
    void updateUserProfile_throwsException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUserProfile("bob", "alice", "alice@a.com"));
        verify(userRepository).findByUsername("bob");
    }

    @Test
    void changePassword_changesPassword_whenOldValid() {
        User user = new User();
        user.setPassword("oldHash");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "oldHash")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("newHash");

        userService.changePassword("bob", "oldPass", "newPass");

        assertEquals("newHash", user.getPassword());
        verify(userRepository).findByUsername("bob");
        verify(passwordEncoder).matches("oldPass", "oldHash");
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);
    }

    @Test
    void changePassword_throwsException_whenOldInvalid() {
        User user = new User();
        user.setPassword("oldHash");
        when(userRepository.findByUsername("bob")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongPass", "oldHash")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.changePassword("bob", "wrongPass", "newPass"));
        verify(userRepository).findByUsername("bob");
        verify(passwordEncoder).matches("wrongPass", "oldHash");
    }

    @Test
    void changePassword_throwsException_whenUserDoesNotExist() {
        when(userRepository.findByUsername("bob")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.changePassword("bob", "oldPass", "newPass"));
        verify(userRepository).findByUsername("bob");
    }
}
