package ru.nikita.cloudrepo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.entity.enums.Role;
import ru.nikita.cloudrepo.exception.UserNotFoundException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthUserDetailsService authUserDetailsService;

    @Test
    void loadUserByUsernameReturnsUserDetails() {
        User user = new User("alice", "encoded-password", Role.ROLE_USER);
        user.setId(7L);
        when(userRepository.findUserByUsername("alice")).thenReturn(Optional.of(user));

        AuthUserDetails details = authUserDetailsService.loadUserByUsername("alice");

        assertEquals("alice", details.getUsername());
        assertEquals("encoded-password", details.getPassword());
        assertEquals("ROLE_USER", details.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsernameThrowsWhenUserMissing() {
        when(userRepository.findUserByUsername("missing")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authUserDetailsService.loadUserByUsername("missing")
        );

        assertEquals("User \"missing\" not found", exception.getMessage());
    }
}

