package ru.rtkmagistral.magistralapi.dto.token;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO, которое отправляется после обработки токена для подтверждения аккаунта сервисом для работы с токенами.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VerifyResponse {
    private boolean status;
    private String message;
}
