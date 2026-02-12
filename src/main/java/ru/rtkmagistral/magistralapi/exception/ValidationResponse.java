package ru.rtkmagistral.magistralapi.exception;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * DTO для ответа при ошибках валидации входных данных.
 */
@Schema(
        name = "ValidationResponse",
        description = "Данные, которые приходят в теле ответа при ошибках валидации."
)
@Data
@AllArgsConstructor
public class ValidationResponse {

    @Schema(
            description = "Сообщение, описывающее причину ошибки. Обычно используется для указания факта ошибки валидации.",
            allowableValues = {
                    "VALIDATION_ERROR"
            },
            example = "VALIDATION_ERROR"
    )
    private String message;

    @Schema(
            description = """
            Map, в котором ключом является название поля, а значением - список описаний ошибок валидации, пришедших на поле.
            """,
            example = "{\"shipping_address\":[\"CANNOT_BE_BLANK\"],\"length\":[\"MUST_BE_GREATER_THAN_0\"]}"
    )
    @JsonProperty("validation_errors")
    private Map<String, List<String>> errors;
}
