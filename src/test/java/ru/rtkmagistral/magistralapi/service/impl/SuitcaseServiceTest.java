package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponseDTO;
import ru.rtkmagistral.magistralapi.dto.suitcase.CreateSuitcaseRequest;
import ru.rtkmagistral.magistralapi.service.spec.IOrdersService;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SuitcaseServiceTest {

    @Mock
    IOrdersService ordersService;

    @InjectMocks
    SuitcaseService suitcaseService;

    private static final UUID KEY = UUID.fromString("b3b6c2a2-9d6d-4c77-9d15-7b3c4bd1a2a9");
    private static final String EMAIL = "vova@example.com";
    private static final String RECEIVER_FIO = "ООО «ЛТК Магистраль»";
    private static final String RECEIVER_PHONE = "+74999999999";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(suitcaseService, "receiverFio", RECEIVER_FIO);
        ReflectionTestUtils.setField(suitcaseService, "receiverPhone", RECEIVER_PHONE);
        ReflectionTestUtils.setField(suitcaseService, "defaultTypeOfShipment", Order.TypeOfShipment.PACKAGE);
        ReflectionTestUtils.setField(suitcaseService, "defaultNatureOfInvestment", NatureOfInvestment.OTHER);
    }

    private CreateSuitcaseRequest validRequest() {
        return new CreateSuitcaseRequest(
                "г. Москва, ул. Тверская, д. 1",
                false,
                "г. Москва, ул. Складочная, д. 1",
                false,
                100, 200, 300,
                1000,
                500_000L,
                "Отвезти в ООО «Ромашка», 3-й этаж.",
                true,
                null,
                true
        );
    }

    @Test
    @DisplayName("createIdempotentSuitcase подставляет реквизиты Магистрали и дефолты, переносит поля чемодана")
    void buildsOrderRequestWithMagistralReceiver() {
        OrderResponseDTO expected = new OrderResponseDTO(
                201, new HttpHeaders(), new OrderResponse("CREATED", true, 1L));
        when(ordersService.createIdempotentOrder(
                eq(KEY), eq(EMAIL), any(CreateOrderRequest.class), eq("POST"), eq("/api/v1/suitcases")
        )).thenReturn(expected);

        OrderResponseDTO actual = suitcaseService.createIdempotentSuitcase(
                KEY, EMAIL, validRequest(), "POST", "/api/v1/suitcases");

        assertThat(actual).isSameAs(expected);

        ArgumentCaptor<CreateOrderRequest> captor = ArgumentCaptor.forClass(CreateOrderRequest.class);
        verify(ordersService).createIdempotentOrder(
                eq(KEY), eq(EMAIL), captor.capture(), eq("POST"), eq("/api/v1/suitcases"));

        CreateOrderRequest sent = captor.getValue();

        // поля чемодана переносятся напрямую, включая адреса забора и доставки
        assertThat(sent.getShippingAddress()).isEqualTo("г. Москва, ул. Тверская, д. 1");
        assertThat(sent.getShippingFromOffice()).isFalse();
        assertThat(sent.getArrivalAddress()).isEqualTo("г. Москва, ул. Складочная, д. 1");
        assertThat(sent.getArrivalToOffice()).isFalse();
        assertThat(sent.getLengthCentiCm()).isEqualTo(100);
        assertThat(sent.getWidthCentiCm()).isEqualTo(200);
        assertThat(sent.getHeightCentiCm()).isEqualTo(300);
        assertThat(sent.getWeightGr()).isEqualTo(1000);
        assertThat(sent.getCostOfInvestmentInKopeika()).isEqualTo(500_000L);
        assertThat(sent.getComment()).isEqualTo("Отвезти в ООО «Ромашка», 3-й этаж.");
        assertThat(sent.getDeliverAsSoonAsPossible()).isTrue();
        assertThat(sent.getWishingDeliveryTime()).isNull();
        assertThat(sent.getAgreeWithTheTermsOfTheAgreement()).isTrue();

        // получатель (контакты) — Магистраль из конфигурации
        assertThat(sent.getReceiverFio()).isEqualTo(RECEIVER_FIO);
        assertThat(sent.getReceiverPhone()).isEqualTo(RECEIVER_PHONE);

        // вид и характер вложения — значения по умолчанию
        assertThat(sent.getTypeOfShipment()).isEqualTo(Order.TypeOfShipment.PACKAGE);
        assertThat(sent.getNatureOfInvestment()).isEqualTo(NatureOfInvestment.OTHER);
    }

    @Test
    @DisplayName("createIdempotentSuitcase делегирует обработку в IOrdersService")
    void delegatesToOrdersService() {
        OrderResponseDTO expected = new OrderResponseDTO(
                201, new HttpHeaders(), new OrderResponse("CREATED", true, 7L));
        when(ordersService.createIdempotentOrder(
                eq(KEY), eq(EMAIL), any(CreateOrderRequest.class), eq("POST"), eq("/api/v1/suitcases")
        )).thenReturn(expected);

        OrderResponseDTO actual = suitcaseService.createIdempotentSuitcase(
                KEY, EMAIL, validRequest(), "POST", "/api/v1/suitcases");

        assertThat(actual.getBody().getAmountOfOrders()).isEqualTo(7L);
        verify(ordersService).createIdempotentOrder(
                eq(KEY), eq(EMAIL), any(CreateOrderRequest.class), eq("POST"), eq("/api/v1/suitcases"));
    }
}
