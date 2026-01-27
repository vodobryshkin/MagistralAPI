package ru.rtkmagistral.magistralapi.validation.formats.uuid;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Locale;

public class UUIDValidator implements ConstraintValidator<UUID, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) return false;

        String normalized = value.toLowerCase(Locale.ROOT);

        try {
            java.util.UUID uuid = java.util.UUID.fromString(normalized);
            return uuid.toString().equals(normalized);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
