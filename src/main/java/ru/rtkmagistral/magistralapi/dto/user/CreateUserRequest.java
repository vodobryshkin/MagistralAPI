package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(
        name = "CreateUserRequest",
        description = "Данные для создания нового пользователя."
)
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
public class CreateUserRequest {
    @Schema(
            description = """
            Имя пользователя. Не может быть пустым и должно соответствовать регулярным выражениям
            "^(?:[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)*|[A-Z][a-z]+(?:-[A-Z][a-z]+)*)$" и "^[а-яА-ЯёЁ]+$".
            """,
            example = "Иван"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @CyrillicWord
    @ProperNoun
    private String name;

    @Schema(
            description = """
            Фамилия пользователя. Не может быть пустой и должна соответствовать регулярным выражениям
            "^(?:[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)*|[A-Z][a-z]+(?:-[A-Z][a-z]+)*)$" и "^[а-яА-ЯёЁ]+$".
            """,
            example = "Иванов"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @CyrillicWord
    @ProperNoun
    private String surname;

    @Schema(
            description = """
            Отчество пользователя. Должно соответствовать регулярным выражениям
            "^(?:[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)*|[A-Z][a-z]+(?:-[A-Z][a-z]+)*)$" и "^[а-яА-ЯёЁ]+$".
            """,
            example = "Иванович"
    )
    @CyrillicWord
    @ProperNoun
    @JsonProperty("fathers_name")
    private String fathersName;

    @Schema(
            description = "Email пользователя. Не может быть пустым и должен соответствовать формату email.",
            example = "user.ivanov.ivanovich.2281337@gmail.com"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Email(message = "MUST_MATCH_FORMAT")
    private String email;

    @Schema(
            description = "Телефон пользователя. Не может быть пустым и должен соответствовать формату +7XXXXXXXXXX.",
            example = "+79999999999"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @PhoneNumber
    private String phone;

    @Schema(
            description = """
            Пароль, который хочет установить пользователь. Не может быть пустым и должен быть от 6 до 32 символов в длину (включительно)
            и соответствовать регулярному выражению "^[\\\\x21-\\\\x7E]+$".
            """,
            example = "12345678"
    )
    @NotBlank(message = "CANNOT_BE_BLANK")
    @Size(min = 6, max = 32, message = "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS")
    @Password
    private String password;

    @Schema(
            description = """
            Согласие на обработку персональных данных. Не может быть пустым и должно быть равно true.
            """,
            allowableValues = {
                    "true"
            }
    )
    @NotNull(message = "CANNOT_BE_NULL")
    @AssertTrue(message = "MUST_BE_TRUE")
    @JsonProperty("agree_to_the_processing_of_personal_data")
    private Boolean agreeOnPersonalDataProcessing;
}
