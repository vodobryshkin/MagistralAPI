package ru.rtkmagistral.magistralapi.validation.formats.phone;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word.CyrillicWordValidator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "Номер мобильного телефона".
 */
@Constraint(validatedBy = CyrillicWordValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface PhoneNumber {
    String message() default "Email does not match the format (+79999999999)";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}