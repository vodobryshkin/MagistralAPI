package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;

/**
 * Интерфейс для определения функциональности ConfirmationLinkService.
 */
public interface IConfirmationLinkService {
    /**
     * Метод для подтверждения корректности ссылки на подтверждение аккаунта.
     *
     * @param id id ссылки.
     */
    VerifyResponse verifyConfirmationLink(String id);

    /**
     * Метод для генерации ссылки на подтверждение аккаунта.
     *
     * @param email адрес электронной почты, с аккаунтом которого будет связана ссылка.
     * @return сгенерированную ссылку.
     */
    ConfirmationLink generateConfirmationLink(String email);

    /**
     * Метод для сохранения в Redis токена на подтверждение аккаунта.
     *
     * @param confirmationLink токен для подтверждения регистрации.
     */
    void saveConfirmationLink(ConfirmationLink confirmationLink);
}
