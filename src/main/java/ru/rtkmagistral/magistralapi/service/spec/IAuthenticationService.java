package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;

/**
 * Интерфейс, который описывает функциональность сервиса для аутентификации в приложении.
 */
public interface IAuthenticationService {
    /**
     * Метод для реализации логики входа в приложение.
     *
     * @param loginRequest запрос на вход в приложение.
     * @return результат входа в приложение.
     */
    AuthResponse login(LoginRequest loginRequest);
}
