package ru.rtkmagistral.magistralapi.validation.rules.cyrillic_word;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять правилу: "Слово состоит только из кириллических букв".
 */
@Constraint(validatedBy = CyrillicWordValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CyrillicWord {
    String message() default "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}