package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.dto.order.OrderResponse;
import ru.rtkmagistral.magistralapi.mapper.IIdempotencyKeyMapper;
import ru.rtkmagistral.magistralapi.repository.IdempotencyKeyRepository;
import ru.rtkmagistral.magistralapi.security.crypto.spec.IHMACEncoder;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IdempotencyKeyServiceTest {

    @Mock
    IHMACEncoder hmacEncoder;
    @Mock
    IdempotencyKeyRepository idempotencyKeyRepository;
    @Mock
    IIdempotencyKeyMapper idempotencyKeyMapper;

    @InjectMocks
    IdempotencyKeyService idempotencyKeyService;

    @Test
    @DisplayName("readIdempotencyKey делегирует репозиторию")
    void readIdempotencyKey_delegates() {
        UUID id = UUID.randomUUID();
        IdempotencyKey key = new IdempotencyKey();
        when(idempotencyKeyRepository.findById(id)).thenReturn(Optional.of(key));

        Optional<IdempotencyKey> result = idempotencyKeyService.readIdempotencyKey(id);

        assertThat(result).containsSame(key);
    }

    @Test
    @DisplayName("createIdempotencyKey ставит статус IN_PROGRESS и сохраняет")
    void createIdempotencyKey_marksInProgressAndSaves() {
        UUID id = UUID.randomUUID();
        IdempotencyKeyDTO dto = new IdempotencyKeyDTO(id, "POST", "/api/v1/orders", "{}");

        IdempotencyKey mapped = new IdempotencyKey();
        mapped.setId(id);
        when(idempotencyKeyMapper.toEntity(dto, hmacEncoder)).thenReturn(mapped);

        IdempotencyKey saved = idempotencyKeyService.createIdempotencyKey(dto);

        assertThat(saved.getIdempotencyKeyStatus())
                .isEqualTo(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS);

        verify(idempotencyKeyRepository).save(saved);
    }

    @Test
    @DisplayName("deactivateIdempotencyKey ставит COMPLETED и записывает данные ответа")
    void deactivateIdempotencyKey_marksCompletedAndFillsResponse() {
        UUID id = UUID.randomUUID();
        IdempotencyKey existing = new IdempotencyKey();
        existing.setId(id);
        existing.setIdempotencyKeyStatus(IdempotencyKey.IdempotencyKeyStatus.IN_PROGRESS);
        when(idempotencyKeyRepository.findById(id)).thenReturn(Optional.of(existing));

        Map<String, String> headers = new HashMap<>();
        headers.put("X-Custom", "value");

        OrderResponse responseBody = new OrderResponse("CREATED", true, 1L);

        idempotencyKeyService.deactivateIdempotencyKey(id, 201, headers, responseBody);

        assertThat(existing.getIdempotencyKeyStatus())
                .isEqualTo(IdempotencyKey.IdempotencyKeyStatus.COMPLETED);
        assertThat(existing.getResponseStatus()).isEqualTo(201);
        assertThat(existing.getResponseHeaders()).isEqualTo(headers);
        assertThat(existing.getResponseBody()).isEqualTo(responseBody);
    }

    @Test
    @DisplayName("deactivateIdempotencyKey бросает исключение если ключ отсутствует")
    void deactivateIdempotencyKey_missing_throws() {
        UUID id = UUID.randomUUID();
        when(idempotencyKeyRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> idempotencyKeyService.deactivateIdempotencyKey(
                id, 201, new HashMap<>(), new OrderResponse("CREATED", true, 1L)))
                .isInstanceOf(java.util.NoSuchElementException.class);
    }
}
