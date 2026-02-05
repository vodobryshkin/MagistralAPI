package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import ru.rtkmagistral.magistralapi.domain.redis.ResendToken;

/**
 * Репозиторий для работы с токенами для повторной отправки токена
 */
@Repository
public interface IResendTokenRepository extends CrudRepository<ResendToken, String> {
}