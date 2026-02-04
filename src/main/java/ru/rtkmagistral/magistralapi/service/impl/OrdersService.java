package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponses;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IOrderMapper;
import ru.rtkmagistral.magistralapi.repository.IOrderRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

/**
 * Сервис для работы с сущностью "Заказ на доставку".
 */
@Service
@RequiredArgsConstructor
public class OrdersService implements IOrdersService {
    private final UserRepository userRepository;
    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;


    /**
     * Метод для создания заказа на доставку в системе.
     *
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param email              адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @return результат создания заказа на доставку.
     */
    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest createOrderRequest, String email) {
        User user = userRepository.findUserByEmail(email)
                .orElseThrow(() -> new UserException("USER_NOT_FOUND"));

        Order order = orderMapper.toEntity(createOrderRequest);
        order.setOrderStatus(Order.OrderStatus.ACCEPTED);
        order.setUser(user);

        orderRepository.save(order);

        return OrderResponses.ORDER_CREATED;
    }
}
