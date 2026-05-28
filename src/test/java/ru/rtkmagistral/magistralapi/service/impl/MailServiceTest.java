package ru.rtkmagistral.magistralapi.service.impl;

import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;

import jakarta.mail.Session;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MailServiceTest {

    @Mock
    JavaMailSender mailSender;
    @Mock
    SpringTemplateEngine templateEngine;

    @InjectMocks
    MailService mailService;

    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailService, "prefix", "https://example.com/verify/");
        ReflectionTestUtils.setField(mailService, "documentTo", "documents@example.com");
        ReflectionTestUtils.setField(mailService, "from", "noreply@example.com");

        mimeMessage = new MimeMessage(Session.getDefaultInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
    }

    @Test
    @DisplayName("sendConfirmationLetter рендерит confirmation-шаблон с правильной ссылкой и шлёт письмо")
    void sendConfirmationLetter_rendersTemplateAndSends() throws Exception {
        when(templateEngine.process(eq("confirmation"), any(Context.class)))
                .thenReturn("<html>hi</html>");

        ConfirmAccountMailRequest req = new ConfirmAccountMailRequest(
                "Владимир", "Александрович", "vova@example.com", "Подтверждение аккаунта");
        req.setLink("abc-123");

        mailService.sendConfirmationLetter(req);

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("confirmation"), ctxCaptor.capture());
        assertThat(ctxCaptor.getValue().getVariable("name")).isEqualTo("Владимир");
        assertThat(ctxCaptor.getValue().getVariable("fathersName")).isEqualTo("Александрович");
        assertThat(ctxCaptor.getValue().getVariable("confirmationLink"))
                .isEqualTo("https://example.com/verify/abc-123");

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Подтверждение аккаунта");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("vova@example.com");
    }

    @Test
    @DisplayName("sendDocumentLetter рендерит document-шаблон и шлёт письмо с вложением на documents@")
    void sendDocumentLetter_rendersTemplateAndAttachesFile() throws Exception {
        when(templateEngine.process(eq("document"), any(Context.class)))
                .thenReturn("<html>doc</html>");

        DocumentMailRequest req = new DocumentMailRequest(
                "Заявка № 42", new byte[]{1, 2, 3}, "Заявка.docx");

        mailService.sendDocumentLetter(req);

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("document"), ctxCaptor.capture());
        assertThat(ctxCaptor.getValue().getVariable("filename")).isEqualTo("Заявка.docx");

        verify(mailSender).send(mimeMessage);
        assertThat(mimeMessage.getSubject()).isEqualTo("Заявка № 42");
        assertThat(mimeMessage.getAllRecipients()[0].toString()).isEqualTo("documents@example.com");
    }

    @Test
    @DisplayName("sendDocumentLetter подставляет имя document.docx если filename пустой")
    void sendDocumentLetter_blankFilename_usesDefault() throws Exception {
        when(templateEngine.process(eq("document"), any(Context.class)))
                .thenReturn("<html>doc</html>");

        DocumentMailRequest req = new DocumentMailRequest("Заявка", new byte[]{1}, "");

        mailService.sendDocumentLetter(req);

        ArgumentCaptor<Context> ctxCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(eq("document"), ctxCaptor.capture());
        assertThat(ctxCaptor.getValue().getVariable("filename")).isEqualTo("document.docx");
    }
}
