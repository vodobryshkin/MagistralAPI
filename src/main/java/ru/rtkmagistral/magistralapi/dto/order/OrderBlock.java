package ru.rtkmagistral.magistralapi.dto.order;

import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;

import java.time.OffsetDateTime;

@Data
public class OrderBlock {
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
