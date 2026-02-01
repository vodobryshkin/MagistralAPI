package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается при ошибке в данных о компании.
 */
public class CompanyCreditsException extends RuntimeException {
    public CompanyCreditsException(String message) {
        super(message);
    }
}
