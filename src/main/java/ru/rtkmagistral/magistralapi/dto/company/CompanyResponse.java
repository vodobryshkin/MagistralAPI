package ru.rtkmagistral.magistralapi.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Schema(
        name = "CompanyResponse",
        description = "Данные, которые приходят в теле ответа при ошибках, связанных с компанией."
)
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class CompanyResponse {

    @Schema(
            description = "Сообщение, описывающее причину ошибки, возникшей при работе с компанией.",
            allowableValues = {
                    "COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA",
                    "INN_NOT_MATCHES_WITH_DADATA",
                    "KPP_NOT_MATCHES_WITH_DADATA",
                    "OKVED_NOT_MATCHES_WITH_DADATA",
                    "TITLE_NOT_MATCHES_WITH_DADATA",
                    "PROBLEMS_WHILE_ADDING_A_COMPANY",
                    "COMPANY_ALREADY_EXISTS_IN_DATABASE",
                    "EMPTY_REQUEST_FOR_DADATA",
                    "CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN",
                    "DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED"
            },
            example = "COMPANY_ALREADY_EXISTS_IN_DATABASE"
    )
    private String message;

    @Hidden
    @JsonProperty("validation_errors")
    private Map<String, List<String>> validationErrors;
}
