package ru.nikita.cloudrepo.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserRequestDto {
    @NotEmpty
    @Size(min=5, max = 25, message = "Username should be between 5 and 25 symbols")
    private String username;

    @NotEmpty
    @Size(min=8, max = 50, message = "Username should be between 5 and 50 symbols")
    private String password;
}
