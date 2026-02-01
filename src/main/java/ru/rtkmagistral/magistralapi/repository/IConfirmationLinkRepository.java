package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rtkmagistral.magistralapi.domain.redis.ConfirmationLink;

import java.util.UUID;

/**
 * Репозиторий для работы с токенами для подтверждения аккаунта
 */
@Repository
public interface IConfirmationLinkRepository extends CrudRepository<ConfirmationLink, UUID> {
}
