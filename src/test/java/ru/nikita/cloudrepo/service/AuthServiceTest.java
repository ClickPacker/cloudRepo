package ru.nikita.cloudrepo.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.nikita.cloudrepo.dto.request.UserRequestDto;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.entity.Role;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private StorageService storageService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MappingService mappingService;

    @Mock
    private PasswordEncoder encoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void signUpBuildsUserAndReturnsMappedResponse() {
        UserRequestDto request = new UserRequestDto();
        request.setUsername("user_1");
        request.setPassword("password123");
        UserResponseDto expected = new UserResponseDto("user_1");

        when(userRepository.findUserByUsername("user_1")).thenReturn(java.util.Optional.empty());
        when(encoder.encode("password123")).thenReturn("encoded-password");
        when(mappingService.toResponseDto(any(User.class))).thenReturn(expected);

        UserResponseDto actual = authService.signUp(request);

        assertSame(expected, actual);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(mappingService).toResponseDto(captor.capture());
        User createdUser = captor.getValue();
        assertEquals("user_1", createdUser.getUsername());
        assertEquals("encoded-password", createdUser.getPassword());
        assertEquals(Role.USER, createdUser.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signUpThrowsConflictWhenUsernameAlreadyExists() {
        UserRequestDto request = new UserRequestDto();
        request.setUsername("user_1");
        request.setPassword("password123");
        User existing = new User();
        existing.setUsername("user_1");

        when(userRepository.findUserByUsername("user_1")).thenReturn(java.util.Optional.of(existing));

        assertThrows(ConflictException.class, () -> authService.signUp(request));
    }
}
