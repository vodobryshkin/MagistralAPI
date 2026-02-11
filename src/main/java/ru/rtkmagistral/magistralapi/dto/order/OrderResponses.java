package ru.rtkmagistral.magistralapi.dto.order;

/**
 * Класс для введения констант для ответов на запрос связанный с заказом на доставку.
 */
public class OrderResponses {
    private OrderResponses() {}

    /**
     * Ответ со статусом 409 Conflict. Заказ до сих пор создаётся.
     */
    public static final OrderResponse ORDER_IS_STILL_BEING_CREATED =
            new OrderResponse("ORDER_IS_STILL_BEING_CREATED", false, null);
}
