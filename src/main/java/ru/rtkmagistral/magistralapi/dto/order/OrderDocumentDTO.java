package ru.rtkmagistral.magistralapi.dto.order;

import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

import java.time.OffsetDateTime;

/**
 * DTO для передачи в сервис для генерации документа.
 */
@Data
public class OrderDocumentDTO {
    private String applicationNumber;
    private OffsetDateTime applicationDate;
    private String contractText;
    private OffsetDateTime pickupDate;
    private OffsetDateTime deliveryDate;
    private int places;
    private String extraInfo;
    private String companyName;

    private UserBlock user;
    private OrderBlock order;

    @Data
    public static class UserBlock {
        private User.UserType userType;
        private String name;
        private String surname;
        private String fathersName;
        private String phone;
    }

    @Data
    public static class OrderBlock {
        private String shippingAddress;
        private String arrivalAddress;

        private int lengthCentiCm;
        private int widthCentiCm;
        private int heightCentiCm;
        private int weightGr;

        private Long costOfInvestmentInKopeika;
        private Order.TypeOfShipment typeOfShipment;
        private NatureOfInvestment natureOfInvestment;

        private boolean secretCargo;
        private boolean deliverAsSoonAsPossible;
        private OffsetDateTime wishingDeliveryTime;

        private String receiverFio;
        private String receiverPhone;
    }
}
