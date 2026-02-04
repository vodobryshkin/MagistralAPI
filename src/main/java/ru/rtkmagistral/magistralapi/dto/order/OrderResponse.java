package ru.rtkmagistral.magistralapi.dto.order;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;

/**
 * DTO для ответа на запрос после совершения какого-либо действия с сущностью "Заказ на доставку".
 */
@Data
@AllArgsConstructor
public class OrderResponse implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private String message;
    private boolean status;
}
