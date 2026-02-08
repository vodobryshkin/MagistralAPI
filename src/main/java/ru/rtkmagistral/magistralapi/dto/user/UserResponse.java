package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO, которое отправляется в сущности ответа на запрос на выполнение операции с пользователем.
 */
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {
    /**
     * Сообщение, которое возвращается в ответ на каждый запрос.
     */
    private String message;
}
