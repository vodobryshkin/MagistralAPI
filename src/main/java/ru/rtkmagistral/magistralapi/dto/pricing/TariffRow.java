package ru.rtkmagistral.magistralapi.dto.pricing;

import java.util.List;

/**
 * Строка тарифной таблицы для одной зоны: фиксированные интервалы веса 0,5–5 кг
 * и пороговые значения с доплатой за каждый килограмм свыше.
 * Значения указаны в рублях без НДС; {@code null} означает, что тариф для зоны недоступен.
 *
 * @param upToHalf  служебная колонка исходной таблицы без заголовка (вес до 0,5 кг считается по отдельной таблице).
 * @param brackets  девять интервалов: (0,5;1], (1;1,5] … (4,5;5] кг.
 * @param per5to10  доплата за каждый кг свыше 5 кг (до 10 кг).
 * @param total10   итоговая стоимость на 10 кг.
 * @param per10to20 доплата за каждый кг свыше 10 кг (до 20 кг).
 * @param total20   итоговая стоимость на 20 кг.
 * @param per20to50 доплата за каждый кг свыше 20 кг (до 50 кг).
 * @param total50   итоговая стоимость на 50 кг.
 * @param perOver50 доплата за каждый кг свыше 50 кг.
 */
public record TariffRow(
        Integer upToHalf,
        List<Integer> brackets,
        Integer per5to10,
        Integer total10,
        Integer per10to20,
        Integer total20,
        Integer per20to50,
        Integer total50,
        Integer perOver50
) {
}
