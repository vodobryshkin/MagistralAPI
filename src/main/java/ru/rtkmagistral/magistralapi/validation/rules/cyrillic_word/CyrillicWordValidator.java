package ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки слова, которое должно состоять только из символов кириллицы (на русском языке)
 */
@Component
public class CyrillicWordValidator implements ConstraintValidator<CyrillicWord, String> {
    private static final Pattern CYRILLIC_WORD = Pattern.compile(
            "^[а-яА-ЯёЁ]+$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return CYRILLIC_WORD.matcher(value).matches();
    }
}
