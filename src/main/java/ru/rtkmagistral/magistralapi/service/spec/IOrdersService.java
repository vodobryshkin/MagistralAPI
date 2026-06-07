package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;

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
    OrderResponseDTO createIdempotentOrder(UUID id,
                                           String email,
                                           CreateOrderRequest createOrderRequest,
                                           String method,
                                           String path);

    /**
     * Метод для идемпотентного создания заказа на доставку с коэффициентом итоговой цены.
     * Стоимость доставки рассчитывается обычным образом и в самом конце домножается на
     * {@code priceMultiplier} (например, 0.95 для заявок на чемодан).
     *
     * @param id ключ идемпотентности.
     * @param email адрес электронной почты пользователя, к которому привязывается заказ.
     * @param createOrderRequest все необходимые данные для создания заказа.
     * @param method http-метод запроса.
     * @param path URI запроса.
     * @param priceMultiplier коэффициент итоговой стоимости доставки.
     * @return результат создания заказа на доставку.
     */
    OrderResponseDTO createIdempotentOrder(UUID id,
                                           String email,
                                           CreateOrderRequest createOrderRequest,
                                           String method,
                                           String path,
                                           double priceMultiplier);
}
