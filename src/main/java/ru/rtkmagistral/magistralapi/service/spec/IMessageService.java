package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;

/**
 * Интерфейс для определения функциональности MessageServiceAspect
 */
public interface IMessageService {
    /**
     * Метод для отправки сообщений на подтверждение аккаунта на RabbitMQ-listener
     */
    void sendConfirmAccountMessageToQueue(ConfirmAccountMailRequest confirmAccountMailRequest);
}
