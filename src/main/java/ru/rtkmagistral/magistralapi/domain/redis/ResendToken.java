package ru.rtkmagistral.magistralapi.domain.redis;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;

/**
 * Сущность "Токен на повторную отправку подтверждения аккаунта."
 */
@Getter
@Setter
@RedisHash("ResendToken")
@AllArgsConstructor
@ToString
public class ResendToken implements Serializable {
    @Id
    private String email;

    @TimeToLive
    private Long ttlSeconds;
}