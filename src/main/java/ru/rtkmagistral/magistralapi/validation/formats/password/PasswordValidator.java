package ru.rtkmagistral.magistralapi.validation.formats.password;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки проверки на правильность символов в пароле.
 */
@Component
public class PasswordValidator implements ConstraintValidator<Password, String> {
    private static final Pattern PHONE = Pattern.compile(
            "^[\\x21-\\x7E]+$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return PHONE.matcher(value).matches();
    }
}
