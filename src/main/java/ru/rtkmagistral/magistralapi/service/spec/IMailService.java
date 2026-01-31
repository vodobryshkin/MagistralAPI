package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;

/**
 * Интерфейс для определения функциональности MailService.
 */
public interface IMailService {
    /**
     * Метод для отправки письма для подтверждения аккаунта.
     *
     * @param confirmAccountMailRequest DTO с данными на отправку письма.
     */
    void sendConfirmationLetter(ConfirmAccountMailRequest confirmAccountMailRequest);
}
