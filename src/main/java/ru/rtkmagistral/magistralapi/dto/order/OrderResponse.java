package ru.rtkmagistral.magistralapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * DTO для ответа на запрос после совершения какого-либо действия с сущностью "Заказ на доставку".
 */
@Value
@AllArgsConstructor
public class OrderResponse {
    String message;
    boolean status;
}
