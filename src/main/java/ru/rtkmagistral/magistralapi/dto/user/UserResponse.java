package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO, которое отправляется в сущности ответа на запрос на выполнение операции с пользователем.
 */
@Data
@AllArgsConstructor
@RequiredArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {
    /**
     * Сообщение, которое возвращается в ответ на каждый запрос.
     */
    private final String message;

    @JsonProperty("user")
    private UserProfileDTO userProfileDTO;
}
