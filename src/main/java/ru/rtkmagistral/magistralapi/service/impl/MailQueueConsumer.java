package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;

@Component
@RequiredArgsConstructor
public class MailQueueConsumer {

    private final MailService mailService;

    @RabbitListener(queues = "${mail.confirm.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void onConfirmAccount(ConfirmAccountMailRequest request) {
        mailService.sendConfirmationLetter(request);
    }
}
