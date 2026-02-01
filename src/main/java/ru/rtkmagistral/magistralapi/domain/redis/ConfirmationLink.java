package ru.rtkmagistral.magistralapi.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;
import java.util.UUID;

/**
 * Сущность "Ссылка для подтверждения аккаунта"
 */
@Getter
@Setter
@RedisHash("ConfirmationLink")
@AllArgsConstructor
@ToString
public class ConfirmationLink implements Serializable {
    @Id
    private UUID id;

    private String username;

    @TimeToLive
    private Long ttlSeconds;
}
