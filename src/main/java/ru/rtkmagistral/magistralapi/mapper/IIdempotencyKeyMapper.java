package ru.rtkmagistral.magistralapi.mapper;

import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.security.crypto.spec.IHMACEncoder;

/**
 * Маппер для перевода между DTO со всей информацией о ключе идемпотентности и сущностью "Ключ идемпотентности".
 */
@Mapper(componentModel = "spring")
public interface IIdempotencyKeyMapper {
    @Mapping(target = "idempotencyKeyStatus", ignore = true)
    @Mapping(target = "responseStatus", ignore = true)
    @Mapping(target = "responseHeaders", ignore = true)
    @Mapping(target = "responseBody", ignore = true)
    @Mapping(target = "httpMethod",
            expression = "java(idempotencyKeyDTO.getHttpMethod() == \"POST\"?" +
                    "ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey.HttpMethod.POST:" +
                    "ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey.HttpMethod.PATCH)")
    @Mapping(target = "requestHash",
            expression = "java(hmacEncoder.mac(idempotencyKeyDTO.getRequestBody()" +
                    ".getBytes(java.nio.charset.StandardCharsets.UTF_8)))")
    IdempotencyKey toEntity(IdempotencyKeyDTO idempotencyKeyDTO,
                            @Context IHMACEncoder hmacEncoder);
}