package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Разобранный сервисом Dadata адрес в объёме, необходимом для тарификации.
 *
 * @param city   город (или ближайший населённый пункт), может быть {@code null}.
 * @param region субъект РФ, может быть {@code null}.
 */
public record DadataAddress(
        String city,
        String region
) {
}
