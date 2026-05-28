package ru.rtkmagistral.magistralapi.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.rtkmagistral.magistralapi.domain.jpa.NatureOfInvestment;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderBlock;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class IOrderMapperTest {

    private final IOrderMapper mapper = Mappers.getMapper(IOrderMapper.class);

    @Test
    @DisplayName("toEntity переносит все поля и оставляет id/user/orderStatus null")
    void toEntity_copiesFields_ignoresIdUserStatus() {
        CreateOrderRequest req = new CreateOrderRequest(
                "г. Москва, ул. Тверская, д. 1",
                "г. Москва, ул. Арбат, д. 10",
                10, 20, 30,
                400,
                500_000L,
                Order.TypeOfShipment.PACKAGE,
                NatureOfInvestment.HOUSEHOLD_CHEMICALS,
                true,
                false,
                OffsetDateTime.parse("2026-08-12T14:30:00+03:00"),
                999L,
                "Иванов Иван Иванович",
                "+79999999999",
                true
        );

        Order order = mapper.toEntity(req);

        assertThat(order.getId()).isNull();
        assertThat(order.getUser()).isNull();
        assertThat(order.getOrderStatus()).isNull();

        assertThat(order.getShippingAddress()).isEqualTo(req.getShippingAddress());
        assertThat(order.getArrivalAddress()).isEqualTo(req.getArrivalAddress());
        assertThat(order.getLengthCentiCm()).isEqualTo(10);
        assertThat(order.getWidthCentiCm()).isEqualTo(20);
        assertThat(order.getHeightCentiCm()).isEqualTo(30);
        assertThat(order.getWeightGr()).isEqualTo(400);
        assertThat(order.getCostOfInvestmentInKopeika()).isEqualTo(500_000L);
        assertThat(order.getTypeOfShipment()).isEqualTo(Order.TypeOfShipment.PACKAGE);
        assertThat(order.getNatureOfInvestment()).isEqualTo(NatureOfInvestment.HOUSEHOLD_CHEMICALS);
        assertThat(order.isSecretCargo()).isTrue();
        assertThat(order.isDeliverAsSoonAsPossible()).isFalse();
        assertThat(order.getWishingDeliveryTime()).isEqualTo(req.getWishingDeliveryTime());
        assertThat(order.getReceiverFio()).isEqualTo("Иванов Иван Иванович");
        assertThat(order.getReceiverPhone()).isEqualTo("+79999999999");
    }

    @Test
    @DisplayName("toDto переносит все нужные поля из Order в OrderBlock")
    void toDto_copiesFields() {
        Order order = new Order();
        order.setShippingAddress("Москва");
        order.setArrivalAddress("Подольск");
        order.setLengthCentiCm(11);
        order.setWidthCentiCm(22);
        order.setHeightCentiCm(33);
        order.setWeightGr(44);
        order.setCostOfInvestmentInKopeika(123L);
        order.setTypeOfShipment(Order.TypeOfShipment.PACKET);
        order.setNatureOfInvestment(NatureOfInvestment.DOCUMENTATION);
        order.setSecretCargo(true);
        order.setDeliverAsSoonAsPossible(false);
        order.setWishingDeliveryTime(OffsetDateTime.parse("2026-08-12T14:30:00+03:00"));
        order.setReceiverFio("Петров П.П.");
        order.setReceiverPhone("+79991112233");

        OrderBlock block = mapper.toDto(order);

        assertThat(block.getShippingAddress()).isEqualTo("Москва");
        assertThat(block.getArrivalAddress()).isEqualTo("Подольск");
        assertThat(block.getLengthCentiCm()).isEqualTo(11);
        assertThat(block.getWidthCentiCm()).isEqualTo(22);
        assertThat(block.getHeightCentiCm()).isEqualTo(33);
        assertThat(block.getWeightGr()).isEqualTo(44);
        assertThat(block.getCostOfInvestmentInKopeika()).isEqualTo(123L);
        assertThat(block.getTypeOfShipment()).isEqualTo(Order.TypeOfShipment.PACKET);
        assertThat(block.getNatureOfInvestment()).isEqualTo(NatureOfInvestment.DOCUMENTATION);
        assertThat(block.isSecretCargo()).isTrue();
        assertThat(block.isDeliverAsSoonAsPossible()).isFalse();
        assertThat(block.getReceiverFio()).isEqualTo("Петров П.П.");
        assertThat(block.getReceiverPhone()).isEqualTo("+79991112233");
    }
}
