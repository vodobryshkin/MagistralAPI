package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

@Schema(
        name = "UserProfileDTO",
        description = "Данные, которые приходят в теле ответа после попытки подтверждения почты."
)
@Data
@AllArgsConstructor
public class UserProfileDTO {
    @Schema(
            description = "Email пользователя.",
            example = "user@example.com"
    )
    private String email;

    @Schema(
            description = "Телефон пользователя.",
            example = "+79999999999"
    )
    private String phone;

    @Schema(
            description = "Тип аккаунта пользователя (обычный или бизнес).",
            example = "INDIVIDUAL"
    )
    private User.UserType userType;

    @Schema(
            description = "Количество заказов, которые совершил пользователь.",
            example = "1"
    )
    @JsonProperty("amount_of_orders")
    private Long amountOfOrders;

    @Schema(
            description = "Информация о том, верифицированный пользователь или нет.",
            example = "true"
    )
    private boolean verified;
}
