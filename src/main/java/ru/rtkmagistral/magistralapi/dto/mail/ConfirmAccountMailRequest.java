package ru.rtkmagistral.magistralapi.dto.mail;

import lombok.AllArgsConstructor;
import lombok.Data;
/**
 * DTO для передачи данных по очереди сообщений на отправку письма по электронной почте
 */
@Data
@AllArgsConstructor
public class ConfirmAccountMailRequest {
    private String name;
    private String surname;
    private String fathersName;
    private String to;
    private String subject;
    private String link;
}
