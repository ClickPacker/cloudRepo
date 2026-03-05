package ru.nikita.cloudrepo.advice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nikita.cloudrepo.exception.ConflictException;
import ru.nikita.cloudrepo.exception.IncorrectPasswordException;
import ru.nikita.cloudrepo.exception.UserDoesNotCreatedException;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ExceptionControllerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new ExceptionController())
                .build();
    }

    @Test
    void handlesUserDoesNotCreatedException() throws Exception {
        mockMvc.perform(get("/throw-user"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("missing user")));
    }

    @Test
    void handlesIncorrectPasswordException() throws Exception {
        mockMvc.perform(get("/throw-password"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("bad password")));
    }

    @Test
    void handlesConflictException() throws Exception {
        mockMvc.perform(get("/throw-conflict"))
                .andExpect(status().isConflict())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("already exists")));
    }

    @RestController
    private static class ThrowingController {
        @GetMapping("/throw-user")
        String throwUserException() {
            throw new UserDoesNotCreatedException("missing user");
        }

        @GetMapping("/throw-password")
        String throwPasswordException() {
            throw new IncorrectPasswordException("bad password");
        }

        @GetMapping("/throw-conflict")
        String throwConflictException() {
            throw new ConflictException("already exists");
        }
    }
}
