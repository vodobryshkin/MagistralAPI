package ru.rtkmagistral.magistralapi.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serial;

/**
 * DTO для ответа на запрос после совершения какого-либо действия с сущностью "Заказ на доставку".
 */
@Schema(
        name = "OrderResponse",
        description = "Данные, которые приходят в теле ответа после выполнения операции над заказом."
)
@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class OrderResponse implements java.io.Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(
            description = "Сообщение, описывающее результат выполненной операции над заказом.",
            allowableValues = {
                    "ORDER_IS_STILL_BEING_CREATED"
            },
            example = "ORDER_CREATED"
    )
    private String message;

    @Schema(
            description = "Переменная, показывающая успех/неудачу выполненной операции над заказом.",
            example = "true"
    )
    private boolean status;

    @Schema(
            description = "Количество заказов пользователя в системе после выполнения операции.",
            example = "5"
    )
    @JsonProperty("amount_of_orders")
    private Long amountOfOrders;
}
