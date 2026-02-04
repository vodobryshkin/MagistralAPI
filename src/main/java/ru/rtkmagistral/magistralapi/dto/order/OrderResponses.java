package ru.rtkmagistral.magistralapi.dto.order;

/**
 * Класс для введения констант для ответов на запрос связанный с заказом на доставку.
 */
public class OrderResponses {
    private OrderResponses() {}

    /**
     * Ответ со статусом 201 Created.
     */
    public static final OrderResponse ORDER_CREATED =
            new OrderResponse("СREATED", true);
}
