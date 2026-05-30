package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.exception.OrderException;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
class OrderControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IOrdersService ordersService;

    @MockitoBean
    IJWTService jwtService;

    private static final String IDEMPOTENCY_KEY = "b3b6c2a2-9d6d-4c77-9d15-7b3c4bd1a2a9";

    private static final String VALID_JSON = """
        {
            "shipping_address": "г. Москва, ул. Тверская, д. 1",
            "arrival_address": "г. Москва, ул. Арбат, д. 10",
            "length": 100,
            "width": 100,
            "height": 100,
            "weight": 1000,
            "cost_of_investment": 100000,
            "type_of_shipment": "PACKAGE",
            "nature_of_investment": "HOUSEHOLD_CHEMICALS",
            "comment": "Хрупкий груз, просьба не кантовать.",
            "deliver_as_soon_as_possible": true,
            "receiver_fio": "Иванов Иван Иванович",
            "receiver_phone": "+79999999999",
            "agree_with_the_terms_of_the_agreement": true
        }
        """;

    @Test
    @DisplayName("POST /orders — валидный запрос возвращает 201 и тело OrderResponse")
    void validOrder_returns201() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(
                201, new HttpHeaders(), new OrderResponse("CREATED", true, 1L));
        when(ordersService.createIdempotentOrder(
                eq(UUID.fromString(IDEMPOTENCY_KEY)),
                eq("vova@example.com"),
                any(),
                any(),
                any()
        )).thenReturn(dto);

        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("CREATED"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.amount_of_orders").value(1));
    }

    @Test
    @DisplayName("POST /orders с категорией FURNITURE (Мебель) возвращает 201")
    void furnitureCategory_returns201() throws Exception {
        OrderResponseDTO dto = new OrderResponseDTO(
                201, new HttpHeaders(), new OrderResponse("CREATED", true, 1L));
        when(ordersService.createIdempotentOrder(
                eq(UUID.fromString(IDEMPOTENCY_KEY)),
                eq("vova@example.com"),
                any(),
                any(),
                any()
        )).thenReturn(dto);

        String json = VALID_JSON.replace(
                "\"nature_of_investment\": \"HOUSEHOLD_CHEMICALS\"",
                "\"nature_of_investment\": \"FURNITURE\"");

        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("CREATED"));
    }

    @Test
    @DisplayName("POST /orders без Idempotency-Key — 400")
    void missingIdempotencyKey_returns400() throws Exception {
        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /orders с одновременно deliver_as_soon_as_possible=true и wishing_delivery_time — 422")
    void conflictingDeliveryFields_returns422() throws Exception {
        String json = """
            {
                "shipping_address": "г. Москва, ул. Тверская, д. 1",
                "arrival_address": "г. Москва, ул. Арбат, д. 10",
                "length": 100,
                "width": 100,
                "height": 100,
                "weight": 1000,
                "cost_of_investment": 100000,
                "type_of_shipment": "PACKAGE",
                "nature_of_investment": "HOUSEHOLD_CHEMICALS",
                "comment": "Хрупкий груз, просьба не кантовать.",
                "deliver_as_soon_as_possible": true,
                "wishing_delivery_time": "2026-08-12T14:30:00+03:00",
                "receiver_fio": "Иванов Иван Иванович",
                "receiver_phone": "+79999999999",
                "agree_with_the_terms_of_the_agreement": true
            }
            """;
        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /orders с нулевыми габаритами — 422")
    void zeroDimensions_returns422() throws Exception {
        String json = """
            {
                "shipping_address": "г. Москва, ул. Тверская, д. 1",
                "arrival_address": "г. Москва, ул. Арбат, д. 10",
                "length": 0,
                "width": 0,
                "height": 0,
                "weight": 0,
                "cost_of_investment": 100000,
                "type_of_shipment": "PACKAGE",
                "nature_of_investment": "HOUSEHOLD_CHEMICALS",
                "comment": "Хрупкий груз, просьба не кантовать.",
                "deliver_as_soon_as_possible": true,
                "receiver_fio": "Иванов Иван Иванович",
                "receiver_phone": "+79999999999",
                "agree_with_the_terms_of_the_agreement": true
            }
            """;
        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /orders с несогласием с договором — 422")
    void agreementMissing_returns422() throws Exception {
        String json = VALID_JSON.replace("\"agree_with_the_terms_of_the_agreement\": true",
                "\"agree_with_the_terms_of_the_agreement\": false");

        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /orders с неверным телефоном получателя — 422")
    void invalidReceiverPhone_returns422() throws Exception {
        String json = VALID_JSON.replace("+79999999999", "12345");

        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnprocessableContent());
    }

    @Test
    @DisplayName("POST /orders когда заказ ещё создаётся — 409 ORDER_IS_STILL_BEING_CREATED")
    void orderStillBeingCreated_returns409() throws Exception {
        when(ordersService.createIdempotentOrder(any(), any(), any(), any(), any()))
                .thenThrow(new OrderException("ORDER_IS_STILL_BEING_CREATED"));

        mvc.perform(post("/api/v1/orders")
                        .with(user("vova@example.com").roles("VERIFIED_USER"))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_JSON))
                .andExpect(status().isConflict());
    }
}
