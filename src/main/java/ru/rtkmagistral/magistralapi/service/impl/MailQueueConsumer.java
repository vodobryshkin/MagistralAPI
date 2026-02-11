package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;
import ru.rtkmagistral.magistralapi.dto.minio.MinioDTO;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.mail.DocumentMailRequest;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IMailService;
import ru.rtkmagistral.magistralapi.service.spec.IMinioService;

@Component
@RequiredArgsConstructor
public class MailQueueConsumer {

    private final IMailService mailService;
    private final IMinioService minioService;
    private final IConfirmationLinkService confirmationLinkService;

    @RabbitListener(queues = "${mail.confirm.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void onConfirmAccount(ConfirmAccountMailRequest request) {
        String id = createConfirmationLink(request.getTo());
        request.setLink(id);

        mailService.sendConfirmationLetter(request);
    }

    @RabbitListener(queues = "${mail.document.queue.name}", containerFactory = "rabbitListenerContainerFactory")
    public void onDocument(DocumentMailRequest request) {
        mailService.sendDocumentLetter(request);
    }

    @RabbitListener(queues = "${minio.queue}", containerFactory = "rabbitListenerContainerFactory")
    public void onMinio(MinioDTO request) {
        MultipartFile mf = new MockMultipartFile(
                "file",
                request.getFilename(),
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                request.getDocument()
        );

        minioService.uploadToOrders(mf);
    }

    private String createConfirmationLink(String email) {
        ConfirmationLink confirmationLink = confirmationLinkService.generateConfirmationLink(email);
        confirmationLinkService.saveConfirmationLink(confirmationLink);

        return confirmationLink.getId().toString();
    }
}
