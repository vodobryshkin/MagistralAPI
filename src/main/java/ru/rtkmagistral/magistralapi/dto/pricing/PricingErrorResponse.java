package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Ответ API при невозможности определить адрес или рассчитать тариф.
 */
public record PricingErrorResponse(
        String message,
        boolean status
) {
}
