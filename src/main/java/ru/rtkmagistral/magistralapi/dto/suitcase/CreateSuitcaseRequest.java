package ru.rtkmagistral.magistralapi.dto.suitcase;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.OffsetDateTime;

/**
 * DTO формы «Чемоданы»: отправитель сдаёт чемодан, получателем выступает Магистраль.
 * Содержит только данные о самом чемодане и отправителе; получатель (Магистраль) подставляется
 * сервисом из конфигурации, поэтому в форме не передаётся.
 */
@Schema(
        name = "CreateSuitcaseRequest",
        description = "Данные для оформления заявки на доставку чемодана в Магистраль."
)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateSuitcaseRequest {
    @Schema(
            description = """
            Адрес, откуда забрать чемодан. Не может быть пустым и должен быть от 1 до 512 символов (включительно).
            """,
            example = "г. Москва, ул. Тверская, д. 1"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(max = 512, message = "LENGTH_MUST_BE_BETWEEN_1_AND_512_SYMBOLS")
    @JsonProperty("shipping_address")
    private String shippingAddress;

    @Schema(
            description = """
            Чемодан сдаётся в отделение спецсвязи («окно»). Если не передано — забор по адресу («дверь»).
            """,
            example = "false"
    )
    @JsonProperty("shipping_from_office")
    private Boolean shippingFromOffice;

    @Schema(
            description = """
            Длина чемодана в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должна быть больше 1 и меньше или равна 20000 (200 см).
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
            Ширина чемодана в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должна быть больше 1 и меньше или равна 20000 (200 см).
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
            Высота чемодана в санти-сантиметрах (в одном сантиметре 100 санти-сантиметров). Должна быть больше 1 и меньше или равна 20000 (200 см).
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
            Вес чемодана в граммах. Должен быть больше 1 и меньше или равен 300000 (300 кг).
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
            Стоимость содержимого чемодана в копейках (в одном рубле 100 копеек). Не может быть null и должна быть больше 0.
            """,
            example = "1000000"
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @Min(value = 1, message = "MUST_BE_GREATER_THAN_0")
    @JsonProperty("cost_of_investment")
    private Long costOfInvestmentInKopeika;

    @Schema(
            description = """
            Произвольный комментарий к заявке. Необязательное поле, не длиннее 1024 символов.
            Желательно указать организацию, в которую нужно отвезти чемодан.
            """,
            example = "Отвезти в ООО «Ромашка», 3-й этаж."
    )
    @Size(max = 1024, message = "LENGTH_MUST_BE_LOWER_OR_EQUAL_THAN_1024_SYMBOLS")
    @JsonProperty("comment")
    private String comment;

    @Schema(
            description = """
            Доставить как можно скорее или нет. Не может быть пустым.
            Если равно true, то поле "wishing_delivery_time" нельзя передавать.
            """,
            example = "false"
    )
    @NotNull(message = "CANNOT_BE_NULL")
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
            Согласие с договором. Не может быть пустым и должно быть равно true.
            """,
            allowableValues = {
                    "true"
            }
    )
    @NotNull(message = "CANNOT_BE_NULL")
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
