package ru.nikita.cloudrepo.service;


import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.UserDoesNotCreatedException;
import ru.nikita.cloudrepo.repository.UserRepository;
import ru.nikita.cloudrepo.repository.entity.User;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    @Transactional
    public void changePassword(String name, String oldPassword, String newPassword) {
        log.info("Change password requested for username={}", name);
        User user = userRepository.findUserByUsername(name)
                .orElseThrow(() -> new UserDoesNotCreatedException("User %s does not exists".formatted(name)));
        if (!encoder.matches(oldPassword, user.getPassword())) {
            log.warn("Change password rejected for username={} due to old password mismatch", name);
            throw new IncorrectPasswordException("Old password doesn't match");
        }
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);
        log.info("Password changed for username={}", name);
    }

}
