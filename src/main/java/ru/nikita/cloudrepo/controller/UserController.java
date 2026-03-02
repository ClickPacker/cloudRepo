package ru.nikita.cloudrepo.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import ru.nikita.cloudrepo.dto.response.UserResponseDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("user")
public class UserController {
    @GetMapping("me")
    private ResponseEntity<UserResponseDto> getMe(@AuthenticationPrincipal UserDetails userDetails) {
        UserResponseDto responseBody = new UserResponseDto(userDetails.getUsername());
        return ResponseEntity
                .status(200)
                .body(responseBody);
    }


}
