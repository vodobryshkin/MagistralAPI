package ru.rtkmagistral.magistralapi.dto.company;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.validation.formats.inn.INN;
import ru.rtkmagistral.magistralapi.validation.formats.kpp.KPP;
import ru.rtkmagistral.magistralapi.validation.formats.okved.OKVED;

/**
 * DTO, которое отправляется в сущности ответа на запрос на добавление компании в систему.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateCompanyRequest {
    @NotBlank
    private String title;

    @NotBlank
    @INN
    private String inn;

    @NotBlank
    @KPP
    private String kpp;

    @NotBlank
    @OKVED
    private String okved;

    @AssertTrue(message = "You must agree to the processing of courier services.")
    @JsonProperty("agree_to_the_processing_of_courier_services")
    private boolean agreeToTheProcessingOfCourierServices;
}
