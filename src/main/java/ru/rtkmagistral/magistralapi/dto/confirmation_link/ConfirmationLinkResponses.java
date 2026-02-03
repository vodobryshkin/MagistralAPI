package ru.rtkmagistral.magistralapi.dto.confirmation_link;

import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;

/**
 * Класс для введения констант для ответов на запрос, связанный с доменной сущностью "Ссылка на подтверждение аккаунта".
 */
public class ConfirmationLinkResponses {
    private ConfirmationLinkResponses() {}

    /**
     * Ответ со статусом 404 Not Found, вызванный тем, что ссылки на подтверждение аккаунта либо не существует, либо её срок годности истёк.
     */
    public static final VerifyResponse CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID =
            new VerifyResponse(false, "CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID");
}
