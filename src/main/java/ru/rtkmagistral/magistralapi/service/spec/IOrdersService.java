package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;

/**
 * Интерфейс для определения функциональности OrdersService.
 */
public interface IOrdersService {
    /**
     * Метод для создания заказа на доставку в системе.
     *
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param email адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @return результат создания заказа на доставку.
     */
    OrderResponse createOrder(CreateOrderRequest createOrderRequest, String email);
}
