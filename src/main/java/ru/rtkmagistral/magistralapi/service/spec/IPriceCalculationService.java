package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationInput;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;

/**
 * Сервис расчёта стоимости экспресс-отправления по правилам из документа «Расчёт стоимости Экспресс».
 */
public interface IPriceCalculationService {
    /**
     * Рассчитывает итоговую стоимость отправления: тариф по зоне и весу с учётом типа доставки,
     * регионального коэффициента, доплаты за отдалённость и сбора за категорию вложения, с НДС и скидкой.
     *
     * @param input подготовленные входные данные расчёта.
     * @return результат расчёта с разбивкой и итоговой ценой в копейках.
     */
    PriceCalculationResult calculate(PriceCalculationInput input);
}
