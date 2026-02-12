package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO, которое отправляется в сущности ответа на запрос на выполнение операции с пользователем.
 */
@Schema (
        name = "UserResponse",
        description = "Данные, которые приходят в теле ответа после попытки регистрации пользователя."
)
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {
    @Schema(
            description = "Сообщение, описывающее прошедшую операцию регистрации.",
            allowableValues = {
                    "USER_WITH_THIS_EMAIL_ALREADY_EXISTS",
                    "USER_WITH_THIS_PHONE_ALREADY_EXISTS",
                    "USER_NOT_FOUND",
                    "CREATED"
            },
            example = "CREATED"
    )
    private final String message;

    @Schema
    @JsonProperty("user")
    private UserProfileDTO userProfileDTO;
}
