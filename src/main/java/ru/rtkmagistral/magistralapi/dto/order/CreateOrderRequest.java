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
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("arrival_address")
    private String arrivalAddress;

    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_THAN_20000")
    @JsonProperty("length")
    private int lengthCentiCm;

    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_THAN_20000")
    @JsonProperty("width")
    private int widthCentiCm;

    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_THAN_20000")
    @JsonProperty("height")
    private int heightCentiCm;

    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_THAN_300000")
    @JsonProperty("weight")
    private int weightGr;

    @NotNull(message = "CANNOT_BE_NULL")
    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("cost_of_investment")
    private Long costOfInvestmentInKopeika;

    @NotNull(message = "CANNOT_BE_NULL")
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
    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("price")
    private Long priceInKopeika;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("receiver_fio")
    private String receiverFio;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @PhoneNumber
    @JsonProperty("receiver_phone")
    private String receiverPhone;

    @AssertTrue(message = "MUST_BE_TRUE")
    @JsonProperty("agree_with_the_terms_of_the_agreement")
    private boolean agreeWithTheTermsOfTheAgreement;

    @AssertTrue(message = "MUST_BE_TRUE")
    public boolean isDeliveryTimeValid() {
        return (deliverAsSoonAsPossible && wishingDeliveryTime == null)
                || (!deliverAsSoonAsPossible && wishingDeliveryTime != null);
    }

}
