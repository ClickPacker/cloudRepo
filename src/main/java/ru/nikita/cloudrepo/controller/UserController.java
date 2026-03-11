package ru.nikita.cloudrepo.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import ru.nikita.cloudrepo.config.security.AuthUserDetails;
import ru.nikita.cloudrepo.dto.request.ChangePasswordRequestDto;
import ru.nikita.cloudrepo.dto.response.AuthResponseDto;
import ru.nikita.cloudrepo.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {
    private final UserService userService;

    @GetMapping("me")
    private ResponseEntity<AuthResponseDto> getMe(@AuthenticationPrincipal AuthUserDetails userDetails) {
        AuthResponseDto responseBody = new AuthResponseDto(userDetails.getUsername());
        return ResponseEntity
                .status(200)
                .body(responseBody);
    }

    @PutMapping("change-password")
    private HttpStatus changePassword(
            @AuthenticationPrincipal AuthUserDetails details,
            @Valid @ModelAttribute ChangePasswordRequestDto changePasswordRequestDto
    ) {
        userService.changePassword(
                details.getUsername(),
                changePasswordRequestDto.getOldPassword(),
                changePasswordRequestDto.getNewPassword()
        );
        return HttpStatus.OK;
    }
}
