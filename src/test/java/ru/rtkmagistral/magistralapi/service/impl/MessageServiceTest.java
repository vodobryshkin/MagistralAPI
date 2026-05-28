package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;
import ru.rtkmagistral.magistralapi.dto.minio.MinioDTO;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

    @Mock
    AmqpTemplate template;

    @InjectMocks
    MessageService messageService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(messageService, "confirmQueueName", "ConfirmQueue");
        ReflectionTestUtils.setField(messageService, "documentQueueName", "DocumentQueue");
        ReflectionTestUtils.setField(messageService, "minioQueueName", "MinioQueue");
    }

    @Test
    @DisplayName("sendConfirmAccountMessageToQueue публикует сообщение в очередь confirm")
    void sendConfirmAccount_routesToConfirmQueue() {
        ConfirmAccountMailRequest req = new ConfirmAccountMailRequest(
                "Владимир", "Александрович", "vova@example.com", "Subj");

        messageService.sendConfirmAccountMessageToQueue(req);

        verify(template).convertAndSend("ConfirmQueue", req);
    }

    @Test
    @DisplayName("sendDocumentMessageToQueue публикует сообщение в очередь document")
    void sendDocumentMessage_routesToDocumentQueue() {
        DocumentMailRequest req = new DocumentMailRequest("Заявка", new byte[]{1, 2, 3}, "file.docx");

        messageService.sendDocumentMessageToQueue(req);

        verify(template).convertAndSend("DocumentQueue", req);
    }

    @Test
    @DisplayName("sendMinioMessageToQueue публикует сообщение в очередь minio")
    void sendMinioMessage_routesToMinioQueue() {
        MinioDTO minio = new MinioDTO(new byte[]{0xA}, "file.docx");

        messageService.sendMinioMessageToQueue(minio);

        verify(template).convertAndSend("MinioQueue", minio);
    }
}
