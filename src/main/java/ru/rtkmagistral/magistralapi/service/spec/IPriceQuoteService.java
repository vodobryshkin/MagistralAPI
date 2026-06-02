package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceCalculationResult;
import ru.rtkmagistral.magistralapi.dto.pricing.PriceQuoteRequest;

/**
 * Сервис предварительного расчёта стоимости доставки. Резолвит города отправления и получения
 * по адресам, после чего применяет тарифную логику {@link IPriceCalculationService}.
 * Используется как отдельным эндпоинтом расчёта, так и при создании заказа/заявки на чемодан.
 */
public interface IPriceQuoteService {
    /**
     * Рассчитывает стоимость по данным предварительного запроса.
     *
     * @param request данные, влияющие на цену.
     * @return результат расчёта с итоговой ценой и расчётным весом.
     */
    PriceCalculationResult quote(PriceQuoteRequest request);

    /**
     * Рассчитывает стоимость по отдельным параметрам отправления.
     *
     * @param shippingAddress         адрес отправления.
     * @param arrivalAddress          адрес получения.
     * @param shippingFromOffice      отправление через отделение (окно), иначе по адресу (дверь).
     * @param arrivalToOffice         получение через отделение (окно), иначе по адресу (дверь).
     * @param weightGr                вес в граммах.
     * @param lengthCentiCm           длина в санти-сантиметрах.
     * @param widthCentiCm            ширина в санти-сантиметрах.
     * @param heightCentiCm           высота в санти-сантиметрах.
     * @param natureOfInvestment      характер вложения (влияет на сбор за категорию).
     * @param costOfInvestmentKopeika оценочная стоимость вложения в копейках.
     * @return итоговая цена отправления в копейках.
     */
    long calculatePriceInKopeika(String shippingAddress,
                                 String arrivalAddress,
                                 Boolean shippingFromOffice,
                                 Boolean arrivalToOffice,
                                 int weightGr,
                                 int lengthCentiCm,
                                 int widthCentiCm,
                                 int heightCentiCm,
                                 NatureOfInvestment natureOfInvestment,
                                 Long costOfInvestmentKopeika);
}
