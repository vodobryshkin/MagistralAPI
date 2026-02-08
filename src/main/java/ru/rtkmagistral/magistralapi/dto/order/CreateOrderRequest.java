package ru.rtkmagistral.magistralapi.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.validation.formats.phone.PhoneNumber;

import java.time.OffsetDateTime;

/**
 * DTO, которое отправляется при добавлении заказа в систему.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateOrderRequest {
    @NotBlank
    @Size(max = 512, message = "Length of shipping address must be less than 512 symnols.")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @NotBlank
    @Size(max = 512, message = "Length of arrival address must be less than 512 symnols.")
    @JsonProperty("arrival_address")
    private String arrivalAddress;

    @Min(value = 0, message = "Length must be greater than 0")
    @Max(value = 20000, message = "Length must be less than 200cm")
    @JsonProperty("length")
    private int lengthCentiCm;

    @Min(value = 0, message = "Width must be greater than 0")
    @Max(value = 20000, message = "Width must be less than 200cm")
    @JsonProperty("width")
    private int widthCentiCm;

    @Min(value = 0, message = "Height must be greater than 0")
    @Max(value = 20000, message = "Height must be less than 200cm")
    @JsonProperty("height")
    private int heightCentiCm;

    @Min(value = 0, message = "Weight must be greater or equal than 0")
    @Max(value = 300000, message = "Weight must be less than 300kg")
    @JsonProperty("weight")
    private int weightGr;

    @NotNull
    @Min(value = 0, message = "Cost of investment must be greater than 0")
    @JsonProperty("cost_of_investment")
    private Long costOfInvestmentInKopeika;

    @NotNull
    @JsonProperty("type_of_shipment")
    private Order.TypeOfShipment typeOfShipment;

    @JsonProperty("nature_of_investment")
    private NatureOfInvestment natureOfInvestment;

    @JsonProperty("secret_cargo")
    private boolean secretCargo;

    @JsonProperty("deliver_as_soon_as_possible")
    private boolean deliverAsSoonAsPossible;

    @JsonProperty("wishing_delivery_time")
    private OffsetDateTime wishingDeliveryTime;

    @NotNull
    @Min(value = 0, message = "Price must me greater than 0")
    @JsonProperty("price")
    private Long priceInKopeika;

    @NotBlank
    @JsonProperty("receiver_fio")
    private String receiverFio;

    @NotBlank
    @PhoneNumber
    @JsonProperty("receiver_phone")
    private String receiverPhone;

    @AssertTrue(message = "You must agree with the terms of agreement.")
    @JsonProperty("agree_with_the_terms_of_the_agreement")
    private boolean agreeWithTheTermsOfTheAgreement;

    @AssertTrue(message = "wishing_delivery_time must be null when deliver_as_soon_as_possible=true, otherwise it must be provided")
    public boolean isDeliveryTimeValid() {
        return (deliverAsSoonAsPossible && wishingDeliveryTime == null)
                || (!deliverAsSoonAsPossible && wishingDeliveryTime != null);
    }

}
