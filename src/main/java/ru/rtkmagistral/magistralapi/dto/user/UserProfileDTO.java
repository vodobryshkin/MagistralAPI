package ru.rtkmagistral.magistralapi.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

@Data
@AllArgsConstructor
public class UserProfileDTO {
    private String email;
    private String phone;
    private User.UserType userType;
    @JsonProperty("amount_of_orders")
    private Long amountOfOrders;
    private boolean verified;
}
