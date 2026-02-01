package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;

/**
 * Класс для отправки запросов на отправку письма по электронной почте в очередь сообщений
 */
@Service
@RequiredArgsConstructor
public class MessageService implements IMessageService {
    private final AmqpTemplate template;

    @Value("${mail.confirm.queue.name}")
    private String confirmQueueName;

    /**
     * Метод для отправки сообщений с данными сервиса отправки почты на RabbitMQ-listener.
     *
     * @param confirmAccountMailRequest запрос на отправку письма для подтверждения аккаунта по электронной почте.
     */
    @Override
    public void sendConfirmAccountMessageToQueue(ConfirmAccountMailRequest confirmAccountMailRequest) {
        template.convertAndSend(confirmQueueName, confirmAccountMailRequest);
    }
}