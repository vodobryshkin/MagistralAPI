package ru.rtkmagistral.magistralapi.dto.pricing;

/**
 * Запись справочника отделений (пунктов) спецсвязи. Используется для определения типа доставки:
 * если адрес стороны совпадает с адресом отделения, доставка считается «через окно».
 *
 * @param name    название отделения/пункта спецсвязи.
 * @param address полный почтовый адрес отделения.
 * @param city    город отделения (выделен из адреса).
 */
public record OfficeEntry(
        String name,
        String address,
        String city
) {
}
