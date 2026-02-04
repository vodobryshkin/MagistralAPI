package ru.rtkmagistral.magistralapi.service.spec;

import org.springframework.http.ResponseEntity;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;

import java.util.UUID;

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

    /**
     * Метод для идемпотентного создания заказа на доставку в системе.
     *
     * @param id ключ идемпотентности.
     * @param method http-метод запроса.
     * @param path URI запроса.
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param email адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @return результат создания заказа на доставку.
     */
    ResponseEntity<OrderResponse> createIdempotentOrder(UUID id,
                                                        String email,
                                                        CreateOrderRequest createOrderRequest,
                                                        String method,
                                                        String path);
}
