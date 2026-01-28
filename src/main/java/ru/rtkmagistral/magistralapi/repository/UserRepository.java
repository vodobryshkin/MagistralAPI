package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

import java.util.UUID;

/**
 * Интерфейс репозитория для работы с сущностью "Пользователь".
 */
public interface UserRepository extends CrudRepository<User, UUID> {
    /**
     * Метод для проверки на то, существует ли пользователь с переданным адресом электронной почты или нет.
     *
     * @param email адрес электронной почты пользователя.
     * @return результат проверки на существование пользователя.
     */
    boolean existsUserByEmail(String email);

    /**
     * Метод для проверки на то, существует ли пользователь с переданным номером телефона или нет.
     *
     * @param phone номер телефона пользователя.
     * @return результат проверки на существование пользователя.
     */
    boolean existsUserByPhone(String phone);
}
