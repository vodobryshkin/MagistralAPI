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
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6, max = 32, message = "Password length must be from 6 to 32 characters")
    @Password
    private String password;
}
