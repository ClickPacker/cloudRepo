package ru.nikita.cloudrepo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import ru.nikita.cloudrepo.dto.request.UserRequestDto;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;
import ru.nikita.cloudrepo.service.AuthService;

import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("auth")
@Slf4j
public class AuthController {
    private final AuthService authService;

    @GetMapping("sign-up")
    private String signUpHandler() {
        return "auth/register";
    }

    @GetMapping("sign-in")
    private String signInHandler() {
        return "auth/login";
    }

    @PostMapping("process-sign-up")
    private ResponseEntity<UserResponseDto> processSignUpHandler(@Valid @ModelAttribute("userRequestDto") UserRequestDto request) {
        UserResponseDto responseDto = authService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(responseDto);
    }

    @PostMapping("sign-out")
    private ResponseEntity<HttpStatus> signOutHandler(@RequestBody Map<String, String> body) {
        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }
}
