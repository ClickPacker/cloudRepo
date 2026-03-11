package ru.nikita.cloudrepo.config.security;

import org.junit.jupiter.api.Test;
import ru.nikita.cloudrepo.entity.enums.Role;
import ru.nikita.cloudrepo.repository.entity.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AuthUserDetailsTest {

    @Test
    void getAuthoritiesReturnsRoleAuthority() {
        User user = new User("alice", "encoded-password", Role.ROLE_ADMIN);
        AuthUserDetails details = new AuthUserDetails(user);

        String authority = details.getAuthorities().iterator().next().getAuthority();

        assertEquals("ROLE_ADMIN", authority);
    }

    @Test
    void getUserBucketUsesUserId() {
        User user = new User("alice", "encoded-password", Role.ROLE_USER);
        user.setId(42L);
        AuthUserDetails details = new AuthUserDetails(user);

        assertEquals("user-42-files", details.getUserBucket());
    }

    @Test
    void returnsUsernameAndPasswordFromUser() {
        User user = new User("bob", "secret", Role.ROLE_USER);
        AuthUserDetails details = new AuthUserDetails(user);

        assertEquals("bob", details.getUsername());
        assertEquals("secret", details.getPassword());
    }
}

