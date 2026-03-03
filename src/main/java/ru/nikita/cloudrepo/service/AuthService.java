package ru.nikita.cloudrepo.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.dto.request.UserRequestDto;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.entity.Role;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.UserNotFoundException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;

import java.util.UUID;


@Service
@Slf4j
@RequiredArgsConstructor

public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final CloudRepositoryService cloudRepositoryService;
    private final UserRepository userRepository;
    private final MappingService mappingService;
    private final PasswordEncoder encoder;

    @Transactional
    public UserResponseDto signUp(UserRequestDto userRequestDto) {
        User user = new User(
                userRequestDto.getUsername(),
                encoder.encode(userRequestDto.getPassword()),
                Role.ROLE_USER);
//        userRepository.save(user);
//        try {
//            cloudRepositoryService.createBucket(userRequestDto.getUsername());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return mappingService.toResponseDto(user);
    }

    public UserResponseDto signIn(UserRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return mappingService
                .toResponseDto(userRepository
                        .findUserByUsername(request.getUsername())
                        .orElseThrow(() -> new UserNotFoundException("User not founded"))
        );
    }

}
