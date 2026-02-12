package ru.rtkmagistral.magistralapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Value;

import java.util.List;

/**
 * DTO, которое отправляется в ответ на запрос, связанный с аутентификацией (эндпойнты /auth/**)
 */
@Schema(
        name = "AuthResponse",
        description = "Данные, которые отправляются в ответ на запрос, связанный с аутентификацией (эндпойнты /auth/**)."
)
@Value
public class AuthResponse {
    @Schema(
            description = "Сообщение, описывающее прошедшую операцию аутентификации.",
            allowableValues = {
                    "INCORRECT_EMAIL_OR_PASSWORD",
                    "REFRESH_TOKEN_INVALID",
                    "CANNOT_IDENTIFY_USER_USING_THIS_REFRESH_TOKEN",
                    "ACCESS_TOKEN_HAS_EXPIRED",
                    "ACCESS_TOKEN_INVALID",
                    "INSUFFICIENT_RIGHTS",
                    "NEED_AUTHENTICATION"
            },
            example = "INCORRECT_EMAIL_OR_PASSWORD"
    )
    String message;
    @JsonIgnore
    List<String> authorities;
}
