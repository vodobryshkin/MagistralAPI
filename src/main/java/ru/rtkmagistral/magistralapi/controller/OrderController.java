package ru.rtkmagistral.magistralapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.security.authorization.ForVerifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

import java.util.UUID;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/orders"
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrdersService ordersService;

    @PostMapping
    @ForVerifiedUsers
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest,
                                                     @RequestHeader("Idempotency-Key") UUID uuid,
                                                     HttpServletRequest httpServletRequest,
                                                     Authentication authentication) {
        OrderResponseDTO orderResponseDTO = ordersService.createIdempotentOrder(
                uuid,
                authentication.getName(),
                createOrderRequest,
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()
        );

        return ResponseEntity
                .status(orderResponseDTO.getCode())
                .headers(orderResponseDTO.getHeaders())
                .body(orderResponseDTO.getBody());
    }
}
