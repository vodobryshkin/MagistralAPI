package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается после неудачной работы с пользователем.
 */
public class UserException extends RuntimeException {
    public UserException(String message) {
        super(message);
    }
}
