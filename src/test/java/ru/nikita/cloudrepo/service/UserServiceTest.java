package ru.nikita.cloudrepo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.UserDoesNotCreatedException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private UserService userService;

    @Test
    void changePasswordThrowsWhenUserDoesNotExist() {
        when(userRepository.findUserByUsername("alice")).thenReturn(Optional.empty());

        assertThrows(UserDoesNotCreatedException.class, () -> userService.changePassword("alice", "old", "new"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordThrowsWhenOldPasswordDoesNotMatch() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-old");
        when(userRepository.findUserByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches("wrong-old", "encoded-old")).thenReturn(false);

        assertThrows(IncorrectPasswordException.class, () -> userService.changePassword("alice", "wrong-old", "new"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changePasswordUpdatesPasswordAndSavesUser() {
        User user = new User();
        user.setUsername("alice");
        user.setPassword("encoded-old");
        when(userRepository.findUserByUsername("alice")).thenReturn(Optional.of(user));
        when(encoder.matches("old", "encoded-old")).thenReturn(true);
        when(encoder.encode("new")).thenReturn("encoded-new");

        userService.changePassword("alice", "old", "new");

        assertEquals("encoded-new", user.getPassword());
        verify(userRepository).save(eq(user));
    }
}
