package ru.rtkmagistral.magistralapi.domain.jpa;

import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;

import java.util.Map;
import java.util.UUID;

/**
 * Доменная сущность "Ключ идемпотентности". Описывает ключ идемпотентности для не идемпотентных запросов.
 */
@Entity
@Table(name = "idempotency_keys")
@NoArgsConstructor
@Getter
@Setter
public class IdempotencyKey {
    /**
     * Enum idempotency_key_status, описанный в DDL.
     */
    public enum IdempotencyKeyStatus {
        COMPLETED,
        IN_PROGRESS
    }

    /**
     * Enum http_method, описанный в DDL. Описываются только два не идемпотентных (согласно RFC) метода: POST и PATCH.
     */
    public enum HttpMethod {
        POST,
        PATCH
    }

    /**
     * Конструктор для сущности "Ключ идемпотентности". В него передаются все поля, которые можно считать с DTO IdempotencyKeyDTO.
     *
     * @param id id ключа идемпотентности.
     * @param path путь запроса.
     * @param requestHash хэш пришедшего тела запроса.
     */
    public IdempotencyKey(UUID id, HttpMethod httpMethod, String path, byte[] requestHash) {
        this.id = id;
        this.httpMethod = httpMethod;
        this.path = path;
        this.requestHash = requestHash;
    }

    /**
     * Уникальный идентификатор пользователя.
     */
    @Id
    private UUID id;

    /**
     * Статус активности ключа идемпотентности. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "status", columnDefinition = "idempotency_key_status", nullable = false)
    private IdempotencyKeyStatus idempotencyKeyStatus;

    /**
     * HTTP-метод запроса, привязанного к ключу идемпотентности. Не может быть null-значением.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "method", columnDefinition = "http_method", nullable = false)
    private HttpMethod httpMethod;

    /**
     * Url запроса, привязанного к ключу идемпотентности. Не может быть null-значением.
     */
    @Column(name = "path", columnDefinition = "TEXT", nullable = false)
    private String path;

    /**
     * Посчитанный хэш тела запроса. Не может быть null-значением.
     */
    @Column(name = "request_hash", nullable = false)
    private byte[] requestHash;

    /**
     * Код ответа на запрос, привязанный к ключу идемпотентности. Изначально null-значение.
     */
    @Column(name = "response_status")
    private Integer responseStatus;

    /**
     * Headers ответа на запрос, привязанный к ключу идемпотентности. Изначально null-значение.
     */
    @Type(JsonType.class)
    @Column(name = "response_headers", columnDefinition = "jsonb")
    private Map<String, String> responseHeaders;

    /**
     * Тело ответа на запрос, привязанный к ключу идемпотентности. Изначально null-значение.
     */
    @Type(JsonType.class)
    @Column(name = "response_body", columnDefinition = "jsonb")
    private UserResponse responseBody;
}
