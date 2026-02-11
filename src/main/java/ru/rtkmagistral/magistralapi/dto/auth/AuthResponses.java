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

    public static final AuthResponse REFRESH_TOKEN_INVALID =
            new AuthResponse("REFRESH_TOKEN_INVALID", null);

    public static final AuthResponse CANNOT_IDENTIFY_USER_USING_THIS_REFRESH_TOKEN =
            new AuthResponse("CANNOT_IDENTIFY_USER_USING_THIS_REFRESH_TOKEN", null);
}
