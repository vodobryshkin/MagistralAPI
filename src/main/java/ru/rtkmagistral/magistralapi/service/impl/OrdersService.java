package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponses;
import ru.rtkmagistral.magistralapi.exception.OrderException;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.mapper.IOrderMapper;
import ru.rtkmagistral.magistralapi.repository.IOrderRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.IIdempotencyKeyService;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с сущностью "Заказ на доставку".
 */
@Service
@RequiredArgsConstructor
public class OrdersService implements IOrdersService {
    private final UserRepository userRepository;
    private final IOrderRepository orderRepository;

    private final IOrderMapper orderMapper;

    private final IIdempotencyKeyService idempotencyKeyService;

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

    /**
     * Метод для идемпотентного создания заказа на доставку в системе.
     *
     * @param id                ключ идемпотентности.
     * @param email              адрес электронной почты пользователя, для нахождения пользователя, к которому следует прикрепить созданный заказ.
     * @param createOrderRequest все необходимые данные для создания заказа на доставку в системе.
     * @param method             http-метод запроса.
     * @param path               URI запроса.
     * @return результат создания заказа на доставку.
     */
    @Override
    @Transactional
    public ResponseEntity<OrderResponse> createIdempotentOrder(UUID id, String email, CreateOrderRequest createOrderRequest, String method, String path) {
        Optional<IdempotencyKey> idempotencyKeyOptional = idempotencyKeyService.readIdempotencyKey(id);

        if (idempotencyKeyOptional.isPresent()) {
            IdempotencyKey idempotencyKey = idempotencyKeyOptional.get();

            if (idempotencyKey.getIdempotencyKeyStatus().equals(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS)) {
                throw new OrderException("ORDER_IS_STILL_BEING_CREATED");
            }

            HttpHeaders headers = new HttpHeaders();
            idempotencyKey.getResponseHeaders().forEach(headers::add);

            return ResponseEntity
                    .status(idempotencyKey.getResponseStatus())
                    .headers(headers)
                    .body(idempotencyKey.getResponseBody());
        }

        IdempotencyKeyDTO idempotencyKeyDTO = new IdempotencyKeyDTO(
                id,
                method,
                path,
                createOrderRequest.toString()
        );

        idempotencyKeyService.createIdempotencyKey(idempotencyKeyDTO);

        OrderResponse orderResponse = createOrder(createOrderRequest, email);

        idempotencyKeyService.deactivateIdempotencyKey(id, 201, new HashMap<>(), orderResponse);

        return ResponseEntity
                .status(201)
                .body(orderResponse);
    }
}
