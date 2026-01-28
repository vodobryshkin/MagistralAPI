package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;

/**
 * Интерфейс, который описывает функциональность класса, отвечающего за бизнес-логику, связанную с работой с пользователями.
 */
public interface IUserService {
    /**
     * Метод для создания пользователя в системе (вызывается при регистрации пользователя)
     *
     * @param createUserRequest данные, необходимые для создания пользователя.
     * @return ответ на создание пользователя.
     */
    UserResponse createUser(CreateUserRequest createUserRequest);
}
