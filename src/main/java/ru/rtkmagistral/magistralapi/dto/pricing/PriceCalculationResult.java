package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Результат расчёта стоимости отправления с разбивкой по основным составляющим.
 *
 * @param priceInKopeika    итоговая цена отправления в копейках (с НДС и скидкой).
 * @param zone              зона доставки по таблице «Зональное распределение».
 * @param deliveryType      применённая тарифная таблица.
 * @param chargeableWeightKg расчётный (больший из фактического и объёмного) вес в килограммах.
 */
public record PriceCalculationResult(
        long priceInKopeika,
        int zone,
        DeliveryType deliveryType,
        double chargeableWeightKg
) {
}
