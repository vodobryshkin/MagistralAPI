package ru.rtkmagistral.magistralapi.dto.mail;

import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO для передачи данных по очереди сообщений на отправку письма по электронной почте
 */
@Data
@RequiredArgsConstructor
public class ConfirmAccountMailRequest {
    private final String name;
    private final String fathersName;
    private final String to;
    private final String subject;
    private String link;
}
