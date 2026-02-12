package ru.rtkmagistral.magistralapi.dto.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;

/**
 * DTO, являющееся телом запроса при добавлении юридического лица в систему.
 */
@Schema(
        name = "CompanyDTO",
        description = "Данные для создания нового юридического лица (пользователь + компания)."
)
@Data
public class CompanyDTO {
    @Schema(
            description = """
            Данные для добавления пользователя. Не могут быть пустыми.
            """
    )
    @Valid
    @NotNull(message = "CANNOT_BE_NULL")
    @JsonProperty("user")
    private CreateUserRequest createUserRequest;

    @Schema(
            description = """
            Данные для добавления компании. Не могут быть пустыми.
            """
    )
    @Valid
    @NotNull(message = "CANNOT_BE_NULL")
    @JsonProperty("company")
    private CreateCompanyRequest createCompanyRequest;
}
