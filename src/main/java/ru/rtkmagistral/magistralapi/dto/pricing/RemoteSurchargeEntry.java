package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Запись о доплате за доставку в отдалённый населённый пункт.
 *
 * @param name  наименование населённого пункта в формате исходной таблицы (регион, пункт, район).
 * @param perKg доплата за каждый килограмм груза в рублях без НДС.
 */
public record RemoteSurchargeEntry(
        String name,
        int perKg
) {
}
