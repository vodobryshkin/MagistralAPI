package ru.rtkmagistral.magistralapi.security.crypto.spec;

/**
 * Интерфейс, который описывает функциональность класса, отвечающего за кодирование/декодирование данных в кодировку HMAC-SHA256
 */
public interface IHMACEncoder {
    /**
     * Метод для подписи данных в кодировку HMAC-SHA256.
     *
     * @param data данные, которые нужно закодировать в кодировку.
     * @return закодированные данные.
     */
    byte[] mac(byte[] data);

    /**
     * Проверяет, что HMAC-подпись (MAC), рассчитанная для входных данных {@code data}, совпадает с ожидаемым значением.
     *
     * @param data исходные данные, для которых вычисляется HMAC.
     * @param expected ожидаемая подпись (MAC) в виде массива байт.
     * @return {@code true}, если вычисленная подпись совпадает с {@code expected}, иначе {@code false}.
     */
    boolean verify(byte[] data, byte[] expected);
}
