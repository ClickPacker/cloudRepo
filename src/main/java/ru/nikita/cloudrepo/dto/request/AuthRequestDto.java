package ru.nikita.cloudrepo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AuthRequestDto {
    @NotBlank(message = "Username is required")
    @Size(min = 5, max = 25, message = "Username should be between 5 and 25 symbols")
    private String username;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 50, message = "Password should be between 8 and 50 symbols")
    private String password;
}
