package ru.rtkmagistral.magistralapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.validation.formats.password.Password;

/**
 * DTO, которое поступает на эндпойнт /auth/login
 */
@Schema(
        name = "LoginRequest",
        description = "Данные для входа в аккаунт."
)
@Data
@AllArgsConstructor
public class LoginRequest {
    @Schema(
            description = "Email пользователя. Не может быть пустым и должен соответствовать формату email.",
            example = "user.ivanov.ivanovich.2281337@gmail.com"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Email(message = "MUST_MATCH_FORMAT")
    private String email;

    @Schema(
            description = """
            Пароль, который хочет установить пользователь. Не может быть пустым и должен быть от 6 до 32 символов в длину (включительно)
            и соответствовать регулярному выражению "^[\\\\\\x21-\\\\\\x7E]+$".
            """,
            example = "12345678"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(min = 6, max = 32, message = "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS")
    @Password
    private String password;
}
