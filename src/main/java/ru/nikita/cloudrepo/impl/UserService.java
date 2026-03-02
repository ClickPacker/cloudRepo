package ru.nikita.cloudrepo.impl;

import ru.nikita.cloudrepo.dto.request.UserRequestDto;
import ru.nikita.cloudrepo.repository.entity.User;

import java.util.Optional;

public interface UserService {
    boolean isUserExists(String username);

    User getUser(Long id);

    Optional<User> getUser(String username);

    User createUser(UserRequestDto user);
    void deleteUser(Long id);
}
