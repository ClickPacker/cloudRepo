package ru.nikita.cloudrepo.service;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.exception.UserNotFoundException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;


@Service
@RequiredArgsConstructor
@Slf4j
public class AuthUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @NotNull
    public AuthUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for username={}", username);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> {
                    log.warn("User not found during authentication username={}", username);
                    return new UserNotFoundException(String.format("User \"%s\" not found", username));
                }
        );
        return new AuthUserDetails(user);
    }
}
