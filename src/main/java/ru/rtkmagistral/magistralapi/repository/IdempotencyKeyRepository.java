package ru.rtkmagistral.magistralapi.repository;

import org.springframework.data.repository.CrudRepository;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;

import java.util.UUID;

/**
 * Интерфейс репозитория для работы с сущностью "Ключ идемпотентности".
 */
public interface IdempotencyKeyRepository extends CrudRepository<IdempotencyKey, UUID> {
}