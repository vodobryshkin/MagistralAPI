package ru.rtkmagistral.magistralapi.security.crypto.impl;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import ru.rtkmagistral.magistralapi.security.crypto.spec.IHMACEncoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * Компонент, который отвечает за кодирование и декодирование данных в кодировку HMAC-SHA256.
 */
@Component
@RequiredArgsConstructor
public class HMACEncoder implements IHMACEncoder {
    @Value("${idempotency.hmac.secret-key}")
    private String key;

    private byte[] keyBytes;

    @PostConstruct
    void init() {
        try {
            this.keyBytes = Base64.getDecoder().decode(key);
        } catch (IllegalArgumentException e) {
            this.keyBytes = key.getBytes(StandardCharsets.UTF_8);
        }
    }

    /**
     * Метод для подписи данных в кодировку HMAC-SHA256.
     *
     * @param data данные, которые нужно закодировать в кодировку.
     * @return закодированные данные.
     */
    @Override
    public byte[] mac(byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(keyBytes, "HmacSHA256"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new IllegalStateException("Cannot compute HMAC-SHA256", e);
        }
    }

    /**
     * Проверяет, что HMAC-подпись (MAC), рассчитанная для входных данных {@code data}, совпадает с ожидаемым значением.
     *
     * @param data исходные данные, для которых вычисляется HMAC.
     * @param expected ожидаемая подпись (MAC) в виде массива байт.
     * @return {@code true}, если вычисленная подпись совпадает с {@code expected}, иначе {@code false}.
     */
    @Override
    public boolean verify(byte[] data, byte[] expected) {
        byte[] actual = mac(data);
        return MessageDigest.isEqual(actual, expected);
    }
}
