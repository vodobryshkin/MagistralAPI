package ru.rtkmagistral.magistralapi.dto.pricing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;

/**
 * DTO для предварительного расчёта стоимости доставки без создания заказа.
 * Содержит только поля, влияющие на цену; реквизиты получателя, согласие с договором и прочие
 * данные оформления здесь не требуются. Подходит для расчёта как обычного заказа, так и чемодана.
 */
@Schema(
        name = "PriceQuoteRequest",
        description = "Данные для предварительного расчёта стоимости доставки."
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PriceQuoteRequest {
    @Schema(
            description = "Адрес отправления. Не может быть пустым и должен быть от 1 до 512 символов (включительно).",
            example = "г. Москва, ул. Тверская, д. 1"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @Schema(
            description = "Отправление сдаётся в отделение спецсвязи («окно»). Если не передано — забор по адресу («дверь»).",
            example = "false"
    )
    @JsonProperty("shipping_from_office")
    private Boolean shippingFromOffice;

    @Schema(
            description = "Адрес получения. Не может быть пустым и должен быть от 1 до 512 символов (включительно).",
            example = "г. Москва, ул. Арбат, д. 10"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("arrival_address")
    private String arrivalAddress;

    @Schema(
            description = "Получение в отделении спецсвязи («окно»). Если не передано — доставка по адресу («дверь»).",
            example = "false"
    )
    @JsonProperty("arrival_to_office")
    private Boolean arrivalToOffice;

    @Schema(
            description = "Длина в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Больше 1 и не более 20000 (200 см).",
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("length")
    private int lengthCentiCm;

    @Schema(
            description = "Ширина в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Больше 1 и не более 20000 (200 см).",
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("width")
    private int widthCentiCm;

    @Schema(
            description = "Высота в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Больше 1 и не более 20000 (200 см).",
            example = "20000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 20000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_20000")
    @JsonProperty("height")
    private int heightCentiCm;

    @Schema(
            description = "Вес в граммах. Больше 1 и не более 300000 (300 кг).",
            example = "300000",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @Max(value = 300000, message = "MUST_BE_LOWER_OR_EQUAL_THAN_300000")
    @JsonProperty("weight")
    private int weightGr;

    @Schema(
            description = "Стоимость вложения в копейках (в одном рубле 100 копеек). Не может быть null и должна быть больше 0.",
            example = "1000000"
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("cost_of_investment")
    private Long costOfInvestmentInKopeika;

    @Schema(
            description = """
            Характер вложения. Необязательное поле: влияет на сбор за ценные категории.
            Если не передано, расчёт выполняется как для категории «Другое».
            """,
            example = "HOUSEHOLD_CHEMICALS"
    )
    @JsonProperty("nature_of_investment")
    private NatureOfInvestment natureOfInvestment;
}
