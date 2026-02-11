package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.validation.formats.password.Password;
import ru.rtkmagistral.magistralapi.validation.formats.phone.PhoneNumber;
import ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word.CyrillicWord;
import ru.rtkmagistral.magistralapi.validation.rules.proper_noun.ProperNoun;

/**
 * DTO, которое отправляется в сущности ответа на запрос на добавление пользователя в систему.
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateUserRequest {
    @NotBlank(message = "CANNOT_BE_BLANK")
    @CyrillicWord
    @ProperNoun
    private String name;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @CyrillicWord
    @ProperNoun
    private String surname;

    @CyrillicWord
    @ProperNoun
    @JsonProperty("fathers_name")
    private String fathersName;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @Email(message = "MUST_MATCH_FORMAT")
    private String email;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @PhoneNumber
    private String phone;

    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(min = 6, max = 32, message = "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS")
    @Password
    private String password;

    @AssertTrue(message = "MUST_BE_TRUE")
    @JsonProperty("agree_to_the_processing_of_personal_data")
    private boolean agreeOnPersonalDataProcessing;
}
