package ru.rtkmagistral.magistralapi.exception;

/**
 * Исключение, которое выбрасывается после попытки создания пользователя с данными, прикреплёнными к другому пользователю.
 */
public class UserWithThisCreditsAlreadyExistsException extends RuntimeException {
    public UserWithThisCreditsAlreadyExistsException(String message) {
        super(message);
    }
}
