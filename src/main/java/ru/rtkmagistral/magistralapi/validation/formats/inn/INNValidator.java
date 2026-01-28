package ru.rtkmagistral.magistralapi.validation.formats.inn;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки ИНН.
 */
@Component
public class INNValidator implements ConstraintValidator<INN, String> {
    private static final Pattern INN_PATTERN = Pattern.compile(
            "^\\d{10}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return INN_PATTERN.matcher(value).matches();
    }
}
