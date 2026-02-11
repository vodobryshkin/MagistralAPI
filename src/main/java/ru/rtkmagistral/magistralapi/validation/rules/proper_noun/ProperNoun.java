package ru.rtkmagistral.magistralapi.validation.rules.proper_noun;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять правилу: "Слово является именем собственным".
 */
@Constraint(validatedBy = ProperNounValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ProperNoun {
    String message() default "MUST_BE_PROPER_NOUN";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}