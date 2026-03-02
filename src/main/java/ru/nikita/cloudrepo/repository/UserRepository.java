package ru.nikita.cloudrepo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nikita.cloudrepo.repository.entity.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User getUserById(Long id);

    Optional<User> findUserByUsername(String username);




}
