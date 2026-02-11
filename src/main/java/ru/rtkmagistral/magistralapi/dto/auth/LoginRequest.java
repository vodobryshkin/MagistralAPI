package ru.rtkmagistral.magistralapi.dto.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.validation.formats.password.Password;

/**
 * DTO, которое поступает на эндпойнт /auth/login
 */
@Data
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Email(message = "MUST_MATCH_FORMAT")
    private String email;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(min = 6, max = 32, message = "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS")
    @Password
    private String password;
}
