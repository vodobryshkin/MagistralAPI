package ru.rtkmagistral.magistralapi.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "CreateOrderRequest",
        description = "Данные для создания нового заказа."
)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateOrderRequest {
    @Schema(
            description = """
            Адрес откуда доставляем заказ. Не может быть пустым и должен быть от 1 до 512 символов (включительно)
            """,
            example = "г. Москва, ул. Тверская, д. 1"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @Schema(
            description = """
            Адрес куда доставляем заказ. Не может быть пустым и должен быть от 1 до 512 символов (включительно)
            """,
            example = "г. Москва, ул. Арбат, д. 10"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("arrival_address")
    private String arrivalAddress;

    @Schema(
            description = """
            Длина посылки в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должен быть больше 1 и меньше или равна 20000 (200 см)
            """,
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("length")
    private int lengthCentiCm;

    @Schema(
            description = """
            Ширина посылки в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должен быть больше 1 и меньше или равна 20000 (200 см)
            """,
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("width")
    private int widthCentiCm;

    @Schema(
            description = """
            Высота посылки в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должен быть больше 1 и меньше или равна 20000 (200 см)
            """,
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("height")
    private int heightCentiCm;

    @Schema(
            description = """
            Вес посылки в граммах. Должен быть больше 1 и меньше или равен 300000 (300 кг).
            """,
            example = "300000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 300000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_300000")
    @JsonProperty("weight")
    private int weightGr;

    @Schema(
            description = """
            Стоимость вложения в копейках (в одном рубле 100 копеек). Не может быть null и должна быть больше 0
            """,
            example = "1000000"
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("cost_of_investment")
    private Long costOfInvestmentInKopeika;

    @Schema(
            description = """
            Вид отправления. Не может быть null и должен принимать только определённые значения".
            """,
            example = "PACKAGE"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("type_of_shipment")
    private Order.TypeOfShipment typeOfShipment;

    @Schema(
            description = """
            Характер вложения. Не может быть null и должен принимать только определённые значения
            """,
            example = "HOUSEHOLD_CHEMICALS"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("nature_of_investment")
    private NatureOfInvestment natureOfInvestment;

    @Schema(
            description = """
            Секретный ли груз или нет. Не может быть null".
            """,
            example = "HOUSEHOLD_CHEMICALS"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("secret_cargo")
    private Boolean secretCargo;

    @Schema(
            description = """
            Доставить как можно скорее или нет. Не может быть пустым.
            Если равно true, то поле "wishing_delivery_time" нельзя передавать.
            """,
            example = "false"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("deliver_as_soon_as_possible")
    private Boolean deliverAsSoonAsPossible;

    @Schema(
            description = """
            Желаемые дата и время получения доставки.
            """,
            format = "date-time",
            example = "2026-02-12T14:30:00+03:00"
    )
    @JsonProperty("wishing_delivery_time")
    private OffsetDateTime wishingDeliveryTime;

    @Schema(
            description = """
            Цена доставки в копейках (в одном рубле 100 копеек). Не может быть null и должна быть больше 0.
            """,
            format = "date-time",
            example = "1000000000"
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @Min(value = 0, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("price")
    private Long priceInKopeika;

    @Schema(
            description = """
            ФИО получателя доставки. Не может быть пустой.
            """,
            format = "date-time",
            example = "Петров Петр Петрович"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @JsonProperty("receiver_fio")
    private String receiverFio;

    @Schema(
            description = "Телефон получателя доставки. Не может быть пустым и должен соответствовать формату +7XXXXXXXXXX.",
            example = "+79999999999"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @PhoneNumber
    @JsonProperty("receiver_phone")
    private String receiverPhone;

    @Schema(
            description = """
            Согласие с договором. Не может быть пустым и должно быть равно true.
            """,
            allowableValues = {
                    "true"
            }
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @AssertTrue(message = "MUST_BE_TRUE")
    @JsonProperty("agree_with_the_terms_of_the_agreement")
    private Boolean agreeWithTheTermsOfTheAgreement;

    @Hidden
    @AssertTrue(message = "MUST_BE_TRUE")
    public boolean isDeliveryTimeValid() {
        return (deliverAsSoonAsPossible && wishingDeliveryTime == null)
                || (!deliverAsSoonAsPossible && wishingDeliveryTime != null);
    }

}
