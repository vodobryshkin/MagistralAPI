package ru.rtkmagistral.magistralapi.dto.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Value;

import java.util.List;

/**
 * DTO, которое отправляется в ответ на запрос, связанный с аутентификацией (эндпойнты /auth/**)
 */
@Value
public class AuthResponse {
    String message;
    @JsonIgnore
    List<String> authorities;
}
