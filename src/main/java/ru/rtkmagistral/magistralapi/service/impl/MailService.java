package ru.rtkmagistral.magistralapi.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.service.spec.IMailService;

import java.nio.charset.StandardCharsets;

/**
 * Сервис для отправки писем электронной почты.
 */
@Service
@RequiredArgsConstructor
public class MailService implements IMailService {
    @Value("${confirmationlink.prefix}")
    private String prefix;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    /**
     * Метод для отправки письма для подтверждения аккаунта.
     *
     * @param confirmAccountMailRequest запрос на отправку письма.
     */
    public void sendConfirmationLetter(ConfirmAccountMailRequest confirmAccountMailRequest) {
        Context context = new Context();
        context.setVariable("name", confirmAccountMailRequest.getName());
        context.setVariable("fathersName", confirmAccountMailRequest.getFathersName());
        context.setVariable("confirmationLink", prefix + confirmAccountMailRequest.getLink());

        String htmlContent = templateEngine.process("confirmation", context);

        sendMessage(confirmAccountMailRequest.getTo(), confirmAccountMailRequest.getSubject(), htmlContent);
    }

    private void sendMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Не удалось отправить письмо", e);
        }
    }
}

