package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается при ошибке во время получения данных с сервиса dadata.
 */
public class DadataClientException extends RuntimeException {
    public DadataClientException(String message) {
        super(message);
    }
}
