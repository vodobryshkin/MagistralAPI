package ru.rtkmagistral.magistralapi.validation.formats.kpp;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки КПП.
 */
@Component
public class KPPValidator implements ConstraintValidator<KPP, String> {
    private static final Pattern KPP_PATTERN = Pattern.compile(
            "^\\d{9}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return KPP_PATTERN.matcher(value).matches();
    }
}
