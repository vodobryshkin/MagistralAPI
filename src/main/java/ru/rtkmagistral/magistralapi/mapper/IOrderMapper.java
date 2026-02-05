package ru.rtkmagistral.magistralapi.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.rtkmagistral.magistralapi.domain.jpa.Order;
import ru.rtkmagistral.magistralapi.dto.order.CreateOrderRequest;
import ru.rtkmagistral.magistralapi.dto.order.OrderBlock;

/**
 * Маппер для перевода между DTO, которое приходит на добавление заказа в систему, и сущностью "Заказ на доставку".
 */
@Mapper(componentModel = "spring")
public interface IOrderMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "orderStatus", ignore = true)
    Order toEntity(CreateOrderRequest createOrderRequest);

    OrderBlock toDto(Order order);
}
