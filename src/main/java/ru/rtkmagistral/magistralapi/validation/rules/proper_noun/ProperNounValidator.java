package ru.rtkmagistral.magistralapi.validation.rules.proper_noun;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

/**
 * Валидатор для обработки слова, которые должны быть именами собственными.
 */
@Component
public class ProperNounValidator implements ConstraintValidator<ProperNoun, String> {
    private static final Pattern PROPER_NOUN = Pattern.compile(
            "^(?:[А-ЯЁ][а-яё]+(?:-[А-ЯЁ][а-яё]+)*|[A-Z][a-z]+(?:-[A-Z][a-z]+)*)$"
    );

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true;
        return PROPER_NOUN.matcher(value).matches();
    }
}
