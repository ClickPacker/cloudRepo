package ru.nikita.cloudrepo.dto.response;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UserResponseDto {
    private final String username;
}
