package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.mapper.IIdempotencyKeyMapper;
import ru.rtkmagistral.magistralapi.repository.IdempotencyKeyRepository;
import ru.rtkmagistral.magistralapi.security.crypto.spec.IHMACEncoder;
import ru.rtkmagistral.magistralapi.service.spec.IIdempotencyKeyService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Сервис для работы с сущностью "Ключ идемпотентности".
 */
@Service
@RequiredArgsConstructor
public class IdempotencyKeyService implements IIdempotencyKeyService {
    private final IHMACEncoder hmacEncoder;

    private final IdempotencyKeyRepository idempotencyKeyRepository;

    private final IIdempotencyKeyMapper idempotencyKeyMapper;

    /**
     * Метод для получения ключа идемпотентности по переданному ID.
     *
     * @param uuid переданный id потенциального ключа идемпотентности.
     * @return null, если ключа с таким id нет, и сам ключ в противном случае.
     */
    @Override
    public Optional<IdempotencyKey> readIdempotencyKey(UUID uuid) {
        return idempotencyKeyRepository.findById(uuid);
    }

    /**
     * Метод для создания ключа идемпотентности после передачи минимальных данных для его создания
     *
     * @param idempotencyKeyDTO данные для создания ключа идемпотентности.
     * @return созданный ключ идемпотентности.
     */
    @Override
    public IdempotencyKey createIdempotencyKey(IdempotencyKeyDTO idempotencyKeyDTO) {
        IdempotencyKey idempotencyKey = idempotencyKeyMapper.toEntity(idempotencyKeyDTO, hmacEncoder);
        idempotencyKey.setIdempotencyKeyStatus(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS);

        idempotencyKeyRepository.save(idempotencyKey);

        return idempotencyKey;
    }

    /**
     * Метод для деактивации ключа идемпотентности после того, как операция завершилась; перевод ключа в режим кэша.
     *
     * @param id id ключа идемпотентности для деактивации.
     * @param statusCode код завершения операции.
     * @param headers заголовки сформированного ответа.
     * @param orderResponse тело сформированного ответа.
     */
    @Override
    @Transactional
    public void deactivateIdempotencyKey(UUID id,
                                         Integer statusCode,
                                         Map<String, String> headers,
                                         OrderResponse orderResponse) {
        IdempotencyKey key = idempotencyKeyRepository.findById(id)
                .orElseThrow();

        key.setResponseStatus(statusCode);
        key.setResponseHeaders(headers);
        key.setResponseBody(orderResponse);
        key.setIdempotencyKeyStatus(IdempotencyKey.IdempotencyKeyStatus.COMPLETED);
    }
}
