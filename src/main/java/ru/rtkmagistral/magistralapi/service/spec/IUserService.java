package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;

/**
 * Интерфейс, который описывает функциональность класса, отвечающего за бизнес-логику, связанную с работой с пользователями.
 */
public interface IUserService {
    /**
     * Метод для создания пользователя (физическое лицо) в системе (вызывается при регистрации пользователя)
     *
     * @param createUserRequest данные, необходимые для создания пользователя.
     * @return ответ на создание пользователя.
     */
    UserResponse createUser(CreateUserRequest createUserRequest);

    /**
     * Метод для создания пользователя (юридическое лицо) в системе (вызывается при регистрации пользователя)
     *
     * @param createUserRequest данные, необходимые для создания пользователя.
     * @param createCompanyRequest данные, необходимые для создания компании.
     * @return ответ на создание пользователя.
     */
    UserResponse createLegalUser(CreateUserRequest createUserRequest, CreateCompanyRequest createCompanyRequest);

    void verifyUser(String email);
}
