package ru.rtkmagistral.magistralapi.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

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
    private String email;

    @Schema(
            description = """
            Пароль, который хочет установить пользователь. Не может быть пустым и должен быть от 6 до 32 символов в длину (включительно)
            и соответствовать регулярному выражению "^[\\\\\\x21-\\\\\\x7E]+$".
            """,
            example = "12345678"
    )
    private String password;
}
