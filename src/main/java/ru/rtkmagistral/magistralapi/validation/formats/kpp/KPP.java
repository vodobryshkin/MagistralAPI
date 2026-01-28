package ru.rtkmagistral.magistralapi.validation.formats.kpp;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Аннотация для полей, которые должны удовлетворять формату: "КПП".
 */
@Constraint(validatedBy = KPPValidator.class)
@Target({ElementType.PARAMETER, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface KPP {
    String message() default "KPP does not match the format.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
