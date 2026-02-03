package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается при ошибке в данных о компании.
 */
public class CompanyException extends RuntimeException {
    public CompanyException(String message) {
        super(message);
    }
}
