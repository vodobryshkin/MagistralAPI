package ru.rtkmagistral.magistralapi.dto.pricing;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO с результатом предварительного расчёта стоимости доставки.
 */
@Schema(
        name = "PriceQuoteResponse",
        description = "Результат расчёта стоимости доставки."
)
@Data
@AllArgsConstructor
public class PriceQuoteResponse {
    @Schema(
            description = "Итоговая стоимость доставки в копейках (с НДС и скидкой).",
            example = "123456"
    )
    @JsonProperty("price_kopeika")
    private Long priceInKopeika;

    @Schema(
            description = "Расчётный вес (больший из фактического и объёмного) в килограммах.",
            example = "1.5"
    )
    @JsonProperty("chargeable_weight_kg")
    private double chargeableWeightKg;
}
