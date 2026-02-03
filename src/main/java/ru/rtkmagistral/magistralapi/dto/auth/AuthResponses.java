package ru.rtkmagistral.magistralapi.dto.auth;

/**
 * Класс для введения констант для ответов на запрос, связанных с аутентификацией.
 */
public class AuthResponses {
    private AuthResponses() {}

    /**
     * Ответ со статусом 401 Unauthorized, вызванный тем, что был передан неправильный логин или пароль.
     */
    public static final AuthResponse INCORRECT_EMAIL_OR_PASSWORD =
            new AuthResponse("INCORRECT_EMAIL_OR_PASSWORD", null);
}
