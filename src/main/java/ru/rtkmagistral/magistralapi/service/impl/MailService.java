package ru.rtkmagistral.magistralapi.service.impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;
import ru.rtkmagistral.magistralapi.service.spec.IMailService;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MailService implements IMailService {
    @Value("${confirmationlink.prefix}")
    private String prefix;

    @Value("${mail.document.mail}")
    private String documentTo;

    @Value("${spring.mail.username}")
    private String from;


    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Override
    public void sendConfirmationLetter(ConfirmAccountMailRequest confirmAccountMailRequest) {
        Context context = new Context();
        context.setVariable("name", confirmAccountMailRequest.getName());
        context.setVariable("fathersName", confirmAccountMailRequest.getFathersName());
        context.setVariable("confirmationLink", prefix + confirmAccountMailRequest.getLink());

        String htmlContent = templateEngine.process("confirmation", context);

        sendMessage(confirmAccountMailRequest.getTo(), confirmAccountMailRequest.getSubject(), htmlContent);
    }

    public void sendDocumentLetter(DocumentMailRequest request) {
        String filename = (request.getFilename() == null || request.getFilename().isBlank())
                ? "document.docx"
                : request.getFilename();

        Context context = new Context();
        context.setVariable("filename", filename);

        String htmlContent = templateEngine.process("document", context);

        sendMessageWithAttachment(
                documentTo,
                request.getSubject(),
                htmlContent,
                request.getDocument(),
                filename
        );
    }

    private void sendMessage(String to, String subject, String htmlContent) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Не удалось отправить письмо", e);
        }
    }

    private void sendMessageWithAttachment(
            String to,
            String subject,
            String htmlContent,
            byte[] attachmentBytes,
            String attachmentFilename
    ) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    true,
                    StandardCharsets.UTF_8.name()
            );

            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            helper.addAttachment(attachmentFilename, new ByteArrayResource(attachmentBytes), "application/vnd.openxmlformats-officedocument.wordprocessingml.document");

            mailSender.send(mimeMessage);
        } catch (MessagingException e) {
            throw new IllegalStateException("Не удалось отправить письмо с документом", e);
        }
    }
}
