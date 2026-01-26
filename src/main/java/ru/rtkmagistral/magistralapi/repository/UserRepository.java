package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rtkmagistral.magistralapi.domain.jpa.User;

import java.util.UUID;

/**
 * Интерфейс репозитория для работы с сущностью "Пользователь".
 */
public interface UserRepository extends CrudRepository<User, UUID> {
}
