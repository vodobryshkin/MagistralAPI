package ru.rtkmagistral.magistralapi.dto.user;

import lombok.Data;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

@Data
public class UserBlock {
    private User.UserType userType;
    private String name;
    private String surname;
    private String fathersName;
    private String phone;
    private String companyName;
}
