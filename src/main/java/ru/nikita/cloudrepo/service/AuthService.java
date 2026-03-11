package ru.nikita.cloudrepo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.dto.request.AuthRequestDto;
import ru.nikita.cloudrepo.dto.response.AuthResponseDto;
import ru.nikita.cloudrepo.entity.enums.Role;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;
import ru.nikita.cloudrepo.service.impl.StorageService;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final PasswordEncoder encoder;

    @Transactional
    public AuthResponseDto signUp(AuthRequestDto authRequestDto) {
        if (userRepository.findUserByUsername(authRequestDto.getUsername()).isPresent()) {
            log.warn("Sign-up conflict for username={}", authRequestDto.getUsername());
            throw new ConflictException("User already exists");
        }

        log.info("Sign-up requested for username={}", authRequestDto.getUsername());
        User user = new User(
                authRequestDto.getUsername(),
                encoder.encode(authRequestDto.getPassword()),
                Role.ROLE_USER);
        userRepository.save(user);
        storageService.getBucketIfExists("user-%d-files".formatted(user.getId()));
        log.info("Sign-up completed for userId={}", user.getId());
        return new AuthResponseDto(user.getUsername());
    }
}
