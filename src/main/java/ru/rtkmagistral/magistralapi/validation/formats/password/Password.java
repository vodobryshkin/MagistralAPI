package ru.rtkmagistral.magistralapi.validation.formats.password;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word.CyrillicWordValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "Пароль пользователя".
 */
@Constraint(validatedBy = CyrillicWordValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Password {
    String message() default "Password must only contain ASCII Symbols";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}