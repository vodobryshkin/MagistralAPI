package ru.rtkmagistral.magistralapi.validation.formats.phone;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки номера мобильного телефона согласно стандарту E.164.
 */
@Component
public class PhoneNumberValidator implements ConstraintValidator<PhoneNumber, String> {
    private static final Pattern PHONE = Pattern.compile(
            "^\\+7\\d{10}$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return PHONE.matcher(value).matches();
    }
}
