package ru.rtkmagistral.magistralapi.mapper;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import ru.rtkmagistral.magistralapi.domain.jpa.IdempotencyKey;
import ru.rtkmagistral.magistralapi.dto.idempotency_key.IdempotencyKeyDTO;
import ru.rtkmagistral.magistralapi.security.crypto.spec.IHMACEncoder;

import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class IIdempotencyKeyMapperTest {
    private final IIdempotencyKeyMapper mapper = Mappers.getMapper(IIdempotencyKeyMapper.class);

    static Stream<Arguments> toEntityCases() {
        return Stream.of(
                Arguments.of(
                        new IdempotencyKeyDTO(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                "POST",
                                "api/v1/key",
                                "CreatekeyRequest()"),
                        new IdempotencyKey(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                IdempotencyKey.HttpMethod.POST,
                                "api/v1/key",
                                "qwerty".getBytes(StandardCharsets.UTF_8)
                        ),
                        true
                ),
                Arguments.of(
                        new IdempotencyKeyDTO(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                "PATCH",
                                "api/v1/key",
                                "CreatekeyRequest()"),
                        new IdempotencyKey(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                IdempotencyKey.HttpMethod.PATCH,
                                "api/v1/key",
                                "qwerty".getBytes(StandardCharsets.UTF_8)
                        ),
                        true
                ),
                Arguments.of(
                        new IdempotencyKeyDTO(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                "POST",
                                "api/v1/key",
                                "CreatekeyRequest()"),
                        new IdempotencyKey(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                IdempotencyKey.HttpMethod.PATCH,
                                "api/v1/key",
                                "qwerty".getBytes(StandardCharsets.UTF_8)
                        ),
                        false
                ),
                Arguments.of(
                        new IdempotencyKeyDTO(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                "PATCH",
                                "api/v1/key",
                                "CreatekeyRequest()"),
                        new IdempotencyKey(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                IdempotencyKey.HttpMethod.PATCH,
                                "api/v1/key/123",
                                "qwerty".getBytes(StandardCharsets.UTF_8)
                        ),
                        false
                ),
                Arguments.of(
                        new IdempotencyKeyDTO(
                                UUID.fromString("550e8400-e29b-41d4-a716-44665540000"),
                                "POST",
                                "api/v1/key",
                                "CreatekeyRequest()"),
                        new IdempotencyKey(
                                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                                IdempotencyKey.HttpMethod.POST,
                                "api/v1/key",
                                "qwerty".getBytes(StandardCharsets.UTF_8)
                        ),
                        false
                )
        );
    }

    @ParameterizedTest(name = "[{index}] {0}")
    @MethodSource("toEntityCases")
    void toEntityTest(IdempotencyKeyDTO dto,
                      IdempotencyKey expected,
                      boolean result) {

        IHMACEncoder encoder = mock(IHMACEncoder.class);
        when(encoder.mac(dto.getRequestBody().getBytes(StandardCharsets.UTF_8))).thenReturn("qwerty".getBytes(StandardCharsets.UTF_8));

        IdempotencyKey key = mapper.toEntity(dto, encoder);

        if (result) {
            assertAll(
                    () -> assertEquals(expected.getId(), key.getId()),
                    () -> assertEquals(expected.getHttpMethod(), key.getHttpMethod()),
                    () -> assertEquals(expected.getPath(), key.getPath()),
                    () -> assertArrayEquals(expected.getRequestHash(), key.getRequestHash()),
                    () -> assertNull(expected.getIdempotencyKeyStatus()),
                    () -> assertNull(expected.getResponseStatus()),
                    () -> assertNull(expected.getResponseHeaders()),
                    () -> assertNull(expected.getResponseBody())
            );
        } else {
            boolean anyMismatch =
                    !java.util.Objects.equals(expected.getId(), key.getId()) ||
                            !java.util.Objects.equals(expected.getHttpMethod(), key.getHttpMethod()) ||
                            !java.util.Objects.equals(expected.getPath(), key.getPath()) ||
                            !java.util.Arrays.equals(expected.getRequestHash(), key.getRequestHash()) ||
                            !java.util.Objects.isNull(expected.getIdempotencyKeyStatus()) ||
                            !java.util.Objects.isNull(expected.getResponseStatus()) ||
                            !java.util.Objects.isNull(expected.getResponseHeaders()) ||
                            !java.util.Objects.isNull(expected.getResponseBody());

            assertTrue(anyMismatch);
        }
    }
}
