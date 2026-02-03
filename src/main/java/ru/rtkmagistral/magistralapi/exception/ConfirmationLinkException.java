package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается после неудачной работы с токеном на подтверждение аккаунта.
 */
public class ConfirmationLinkException extends RuntimeException {
    public ConfirmationLinkException(String message) {
        super(message);
    }
}
