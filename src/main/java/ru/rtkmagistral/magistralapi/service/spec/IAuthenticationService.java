package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;
import ru.rtkmagistral.magistralapi.dto.resend_token.ResendTokenDTO;

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

    /**
     * Метод для проверки на то, можно ли сейчас отправить новое письмо с подтверждением аккаунта.
     *
     * @param email адрес электронной почты пользователя.
     * @return сформированный респонс энтити.
     */
    ResendTokenDTO resend(String email);

    /**
     * Метод для создания токена для контроля когда можно отправить новое письмо с подтверждением аккаунта.
     *
     * @param email адрес электронной почты пользователя.
     */
    void createResendToken(String email);
}
