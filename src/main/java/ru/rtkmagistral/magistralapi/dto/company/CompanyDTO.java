package ru.rtkmagistral.magistralapi.dto.company;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import lombok.Data;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;

/**
 * DTO, являющееся телом запроса при добавлении юридического лица в систему.
 */
@Data
public class CompanyDTO {
    @Valid
    @JsonProperty("user")
    private CreateUserRequest createUserRequest;

    @Valid
    @JsonProperty("company")
    private CreateCompanyRequest createCompanyRequest;
}
