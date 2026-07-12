package ru.rtkmagistral.magistralapi.dto.pricing;

import java.util.List;

/**
 * Сопоставление субъекта РФ, возвращаемого Dadata, с городом тарифной матрицы.
 *
 * @param regionIsoCode    ISO-код субъекта из {@code region_iso_code} Dadata.
 * @param regionNames      допустимые значения/варианты поля {@code region} Dadata.
 * @param tariffCity       город-ключ в {@code pricing/zones.json}.
 * @param coefficientPolicy правило определения регионального коэффициента.
 */
public record TariffRegionMapping(
        String regionIsoCode,
        List<String> regionNames,
        String tariffCity,
        CoefficientPolicy coefficientPolicy
) {
}
