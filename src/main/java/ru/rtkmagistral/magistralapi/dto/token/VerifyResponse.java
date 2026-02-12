package ru.rtkmagistral.magistralapi.dto.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO, которое отправляется после обработки токена для подтверждения аккаунта сервисом для работы с токенами.
 */
@Schema(
        name = "VerifyResponse",
        description = "Данные, которые приходят в теле ответа после попытки подтверждения почты."
)
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResponse {
    @Schema(
            description = "Переменная, показывающая успех/неудачу прошедшей операции подтверждения почты",
            example = "true"
    )
    private boolean status;

    @Schema(
            description = "Сообщение, описывающее прошедшую операцию подтверждения почты. Если вернулся 2xx ответ, то message=null.",
            allowableValues = {
                    "CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID",
                    "CREATED"
            },
            example = "CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID"
    )
    private String message;
}
