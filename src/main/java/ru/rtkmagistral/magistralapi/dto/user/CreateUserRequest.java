package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.*;
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
public class CreateUserRequest {
    @NotBlank
    @CyrillicWord
    @ProperNoun
    private String name;

    @NotBlank
    @CyrillicWord
    @ProperNoun
    private String surname;

    @CyrillicWord
    @ProperNoun
    @JsonProperty("fathers_name")
    private String fathersName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @PhoneNumber
    private String phone;

    @NotBlank
    @Size(min = 6, max = 32, message = "Password length must be from 6 to 32 characters")
    @Password
    private String password;

    @AssertTrue(message = "You must agree to the processing of personal data")
    @JsonProperty("agree_to_the_processing_of_personal_data")
    private boolean agreeOnPersonalDataProcessing;
}
