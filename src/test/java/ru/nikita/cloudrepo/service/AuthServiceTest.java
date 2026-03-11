package ru.nikita.cloudrepo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nikita.cloudrepo.dto.request.AuthRequestDto;
import ru.nikita.cloudrepo.dto.response.AuthResponseDto;
import ru.nikita.cloudrepo.entity.enums.Role;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;
import ru.nikita.cloudrepo.service.impl.StorageService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signUpBuildsUserAndReturnsMappedResponse() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("user_1");
        request.setPassword("password123");
        AuthResponseDto expected = new AuthResponseDto("user_1");

        when(userRepository.findUserByUsername("user_1")).thenReturn(java.util.Optional.empty());
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(101L);
            return user;
        });

        AuthResponseDto actual = authService.signUp(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        verify(storageService).getBucketIfExists("user-101-files");

        User savedUser = userCaptor.getValue();
        assertEquals("user_1", savedUser.getUsername());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.ROLE_USER, savedUser.getRole());

        assertEquals(expected.getUsername(), actual.getUsername());
    }

    @Test
    void signUpThrowsConflictWhenUsernameAlreadyExists() {
        AuthRequestDto request = new AuthRequestDto();
        request.setUsername("user_1");
        request.setPassword("password123");
        User existing = new User();
        existing.setUsername("user_1");

        when(userRepository.findUserByUsername("user_1")).thenReturn(java.util.Optional.of(existing));

        assertThrows(ConflictException.class, () -> authService.signUp(request));
        verifyNoInteractions(storageService, encoder);
    }
}
