package ru.nikita.cloudrepo.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.nikita.cloudrepo.entity.enums.Role;
import ru.nikita.cloudrepo.repository.entity.User;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.liquibase.enabled=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryTestcontainersTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("cloud_repo_test")
            .withUsername("cloud_repo_user")
            .withPassword("cloud_repo_password");

    @DynamicPropertySource
    static void registerDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.datasource.driver-class-name", postgres::getDriverClassName);
    }

    @Autowired
    private UserRepository userRepository;

    @Test
    void saveAndFindUserByUsername() {
        User user = new User("alice", "encoded-password", Role.ROLE_USER);
        userRepository.saveAndFlush(user);

        var actual = userRepository.findUserByUsername("alice");

        assertThat(actual).isPresent();
        assertThat(actual.get().getId()).isNotNull();
        assertThat(actual.get().getUsername()).isEqualTo("alice");
        assertThat(actual.get().getRole()).isEqualTo(Role.ROLE_USER);
    }

    @Test
    void getUserByIdReturnsPersistedEntity() {
        User saved = userRepository.saveAndFlush(new User("bob", "secret", Role.ROLE_ADMIN));

        User actual = userRepository.getUserById(saved.getId());

        assertThat(actual).isNotNull();
        assertThat(actual.getId()).isEqualTo(saved.getId());
        assertThat(actual.getUsername()).isEqualTo("bob");
        assertThat(actual.getRole()).isEqualTo(Role.ROLE_ADMIN);
    }

    @Test
    void saveAndFlushFailsForDuplicateUsername() {
        userRepository.saveAndFlush(new User("duplicate-user", "pass-1", Role.ROLE_USER));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new User("duplicate-user", "pass-2", Role.ROLE_ADMIN))
        ).isInstanceOf(DataIntegrityViolationException.class);
    }
}
