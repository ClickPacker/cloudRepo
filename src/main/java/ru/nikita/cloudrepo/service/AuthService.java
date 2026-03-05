package ru.nikita.cloudrepo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.dto.request.UserRequestDto;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.entity.Role;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final StorageService storageService;
    private final UserRepository userRepository;
    private final MappingService mappingService;
    private final PasswordEncoder encoder;

    @Transactional
    public UserResponseDto signUp(UserRequestDto userRequestDto) {
        if (userRepository.findUserByUsername(userRequestDto.getUsername()).isPresent()) {
            log.warn("Sign-up conflict for username={}", userRequestDto.getUsername());
            throw new ConflictException("User already exists");
        }

        log.info("Sign-up requested for username={}", userRequestDto.getUsername());
        User user = new User(
                userRequestDto.getUsername(),
                encoder.encode(userRequestDto.getPassword()),
                Role.USER);
        userRepository.save(user);
        storageService.createBucket(user.getId());
        log.info("Sign-up completed for userId={}", user.getId());
        return mappingService.toResponseDto(user);
    }
}
