package ru.rtkmagistral.magistralapi.dto.user;

/**
 * Класс для введения констант для ответов на запрос.
 */
public final class UserResponses {
    private UserResponses() {}

    /**
     * Ответ со статусом 201 Created.
     */
    public static final UserResponse USER_CREATED =
            new UserResponse("СREATED", null);

    /**
     * Ответ со статусом 409 Conflict, вызванный совпадением адреса электронной почти.
     */
    public static final UserResponse USER_WITH_THIS_EMAIL_ALREADY_EXISTS =
            new UserResponse("USER_WITH_THIS_EMAIL_ALREADY_EXISTS", null);

    /**
     * Ответ со статусом 409 Conflict, вызванный совпадением номера телефона.
     */
    public static final UserResponse USER_WITH_THIS_PHONE_ALREADY_EXISTS =
            new UserResponse("USER_WITH_THIS_PHONE_ALREADY_EXISTS", null);

    /**
     * Ответ со статусом 400 Bad Request.
     */
    public static final UserResponse BAD_REQUEST =
            new UserResponse("BAD_REQUEST", null);

    /**
     * Ответ со статусом 404 Not Found, вызванный невоможностью найти пользователя по переданным идентификационным данным.
     */
    public static final UserResponse USER_NOT_FOUND =
            new UserResponse("USER_NOT_FOUND", null);
}

