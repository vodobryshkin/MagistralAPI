package ru.rtkmagistral.magistralapi.service.spec;

import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Интерфейс для определения функциональности IdempotencyKeyService.
 */
public interface IIdempotencyKeyService {
    /**
     * Метод для получения ключа идемпотентности по переданному ID.
     *
     * @param uuid переданный id потенциального ключа идемпотентности.
     * @return null, если ключа с таким id нет, и сам ключ в противном случае.
     */
    Optional<IdempotencyKey> readIdempotencyKey(UUID uuid);

    /**
     * Метод для создания ключа идемпотентности после передачи минимальных данных для его создания
     *
     * @param idempotencyKeyDTO данные для создания ключа идемпотентности.
     * @return созданный ключ идемпотентности.
     */
    IdempotencyKey createIdempotencyKey(IdempotencyKeyDTO idempotencyKeyDTO);

    /**
     * Метод для деактивации ключа идемпотентности после того, как операция завершилась; перевод ключа в режим кэша.
     *
     * @param id id ключа идемпотентности для деактивации.
     * @param statusCode код завершения операции.
     * @param headers заголовки сформированного ответа.
     * @param orderResponse тело сформированного ответа.
     */
    void deactivateIdempotencyKey(UUID id,
                                  Integer statusCode,
                                  Map<String, String> headers,
                                  OrderResponse orderResponse);
}
