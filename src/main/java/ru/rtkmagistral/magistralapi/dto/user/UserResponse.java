package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO, которое отправляется в сущности ответа на запрос на добавление пользователя в систему.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class UserResponse {
    /**
     * Сообщение, которое возвращается в ответ на каждый запрос.
     */
    private String message;

    /**
     * Map, в котором ключом является название поля, а значением описания ошибок валидации, пришедших на поле.
     * Если сообщение не "VALIDATION_ERROR", то это поле будет null'ом.
     */
    @JsonProperty("validation_errors")
    private Map<String, List<String>> validationErrors;
}
