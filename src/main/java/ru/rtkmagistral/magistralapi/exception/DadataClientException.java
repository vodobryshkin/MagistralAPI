package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение при ошибке обращения к Dadata или разбора её ответа.
 */
public class DadataClientException extends RuntimeException {
    public DadataClientException(String message) {
        super(message);
    }

    public DadataClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
