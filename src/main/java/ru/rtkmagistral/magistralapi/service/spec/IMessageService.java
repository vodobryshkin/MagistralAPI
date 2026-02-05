package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;

/**
 * Интерфейс для определения функциональности MessageServiceAspect
 */
public interface IMessageService {
    /**
     * Метод для отправки сообщений на подтверждение аккаунта на RabbitMQ-listener
     */
    void sendConfirmAccountMessageToQueue(ConfirmAccountMailRequest confirmAccountMailRequest);

    /**
     * Метод для отправки сообщений с данными сервиса отправки почты на RabbitMQ-listener.
     *
     * @param documentMailRequest запрос на отправку письма со сформированным документом по заявке по электронной почте.
     */
    void sendDocumentMessageToQueue(DocumentMailRequest documentMailRequest);
}
