package ru.rtkmagistral.magistralapi.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.validation.formats.inn.INN;
import ru.rtkmagistral.magistralapi.validation.formats.kpp.KPP;
import ru.rtkmagistral.magistralapi.validation.formats.okved.OKVED;

/**
 * DTO, которое отправляется в сущности ответа на запрос на добавление компании в систему.
 */
@Schema(
        name = "CreateCompanyRequest",
        description = "Данные для создания компании."
)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateCompanyRequest {
    @Schema (
            description = """
                    Название компании. Не может быть пустым.
                    """,
            example = "ООО \\\"ЯНДЕКС\\\""
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    private String title;

    @Schema (
            description = """
                    ИНН компании. Не может быть пустым и должен соответствовать регулярному выражению "^\\\\d{10}$".
                    """,
            example = "7736207543"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @INN
    private String inn;

    @Schema (
            description = """
                    КПП компании. Не может быть пустым и должен соответствовать регулярному выражению "^\\\\d{9}$".
                    """,
            example = "770401001"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @KPP
    private String kpp;

    @Schema (
            description = """
                    ОКВЭД компании. Не может быть пустым и должен соответствовать регулярному выражению
                    "^\\\\d{2}(?:\\\\.\\\\d|\\\\.\\\\d{2}(?:\\\\.\\\\d|\\\\.\\\\d{2})?)?$".
                    """,
            example = "62.01"
    )
    @OKVED
    private String okved;

    @Schema (
            description = """
                    Согласие на курьерские услуги. Не может быть пустым и должно быть равно true.
                    """,
            allowableValues = {
                    "true"
            }
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @AssertTrue(message = "MUST_BE_TRUE")
    @JsonProperty("agree_to_the_processing_of_courier_services")
    private Boolean agreeToTheProcessingOfCourierServices;
}
