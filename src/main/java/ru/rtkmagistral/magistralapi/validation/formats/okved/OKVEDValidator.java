package ru.rtkmagistral.magistralapi.validation.formats.okved;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки ОКВЭД.
 */
@Component
public class OKVEDValidator implements ConstraintValidator<OKVED, String> {
    private static final Pattern OKVED_PATTERN = Pattern.compile(
            "^\\d{2}(?:\\.\\d|\\.\\d{2}(?:\\.\\d|\\.\\d{2})?)?$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return OKVED_PATTERN.matcher(value).matches();
    }
}
