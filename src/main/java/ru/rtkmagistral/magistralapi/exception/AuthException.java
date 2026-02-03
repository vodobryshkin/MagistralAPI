package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается при ошибке во время работы с какой-либо функциональностью аутентификации.
 */
public class AuthException extends RuntimeException {
    public AuthException(String message) {
        super(message);
    }
}
