package ru.rtkmagistral.magistralapi.dto.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * DTO для передачи данных по очереди сообщений на отправку письма по электронной почте
 */
@Data
@AllArgsConstructor
public class DocumentMailRequest {
    private String subject;
    private byte[] document;
    private String filename;
}
