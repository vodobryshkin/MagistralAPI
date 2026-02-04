package ru.rtkmagistral.magistralapi.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
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
    @PreAuthorize("hasRole('VERIFIED_USER')")
    public ResponseEntity<OrderResponse> createOrder(@RequestBody @Valid CreateOrderRequest createOrderRequest,
                                                     @RequestHeader("Idempotency-Key") UUID uuid,
                                                     HttpServletRequest httpServletRequest,
                                                     Authentication authentication) {
        return ordersService.createIdempotentOrder(
                uuid,
                authentication.getName(),
                createOrderRequest,
                httpServletRequest.getMethod(),
                httpServletRequest.getRequestURI()
        );
    }
}
