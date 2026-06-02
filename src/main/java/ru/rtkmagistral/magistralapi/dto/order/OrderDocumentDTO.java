package ru.rtkmagistral.magistralapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.dto.user.UserBlock;

import java.time.OffsetDateTime;

/**
 * DTO для передачи в сервис для генерации документа.
 */
@Data
@AllArgsConstructor
public class OrderDocumentDTO {
    private String applicationNumber;
    private OffsetDateTime applicationDate;
    private String contractText;
    private OffsetDateTime pickupDate;
    private OffsetDateTime deliveryDate;
    private int places;
    private String extraInfo;
    private Long priceInKopeika;

    private UserBlock user;
    private OrderBlock order;
}
