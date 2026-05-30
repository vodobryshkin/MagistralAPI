package ru.rtkmagistral.magistralapi.dto.pricing;

import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;

/**
 * Подготовленные входные данные для расчёта стоимости отправления.
 * Города уже приведены к названиям из таблицы зон, а региональные коэффициенты и доплаты
 * за отдалённость — определены на этапе резолвинга адресов.
 *
 * @param senderCity         город отправления (наименование из таблицы «Зональное распределение»).
 * @param receiverCity       город получения (наименование из таблицы «Зональное распределение»).
 * @param senderIsOffice     отправление через отделение спецсвязи (окно), иначе по адресу (дверь).
 * @param receiverIsOffice   получение через отделение спецсвязи (окно), иначе по адресу (дверь).
 * @param weightGr           фактический вес груза в граммах.
 * @param lengthCentiCm      длина груза в санти-сантиметрах.
 * @param widthCentiCm       ширина груза в санти-сантиметрах.
 * @param heightCentiCm      высота груза в санти-сантиметрах.
 * @param natureOfInvestment характер вложения (определяет необходимость сбора за категорию).
 * @param declaredValueKopeika оценочная стоимость вложения в копейках.
 * @param senderCoefficient  региональный коэффициент по адресу отправления (1,0 / 1,15 / 1,25).
 * @param receiverCoefficient региональный коэффициент по адресу получения (1,0 / 1,15 / 1,25).
 * @param senderRemotePerKg  доплата за отдалённость по адресу отправления, руб./кг; {@code null} — нет.
 * @param receiverRemotePerKg доплата за отдалённость по адресу получения, руб./кг; {@code null} — нет.
 */
public record PriceCalculationInput(
        String senderCity,
        String receiverCity,
        boolean senderIsOffice,
        boolean receiverIsOffice,
        int weightGr,
        int lengthCentiCm,
        int widthCentiCm,
        int heightCentiCm,
        NatureOfInvestment natureOfInvestment,
        long declaredValueKopeika,
        double senderCoefficient,
        double receiverCoefficient,
        Integer senderRemotePerKg,
        Integer receiverRemotePerKg
) {
}
