package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/orders"
 */
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {
    private final IOrdersService ordersService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('VERIFIED_USER')")
    public OrderResponse createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest, Authentication authentication) {
        return ordersService.createOrder(createOrderRequest, authentication.getName());
    }
}
