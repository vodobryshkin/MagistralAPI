package ru.rtkmagistral.magistralapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserResponses;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Класс, который отвечает за перехват и обработку всех выкинутых в программе исключений.
 */
@RestControllerAdvice
public class AppExceptionHandler {
    /**
     * Метод для обработки исключения UserWithThisCreditsAlreadyExistsException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
     */
    @ExceptionHandler(UserWithThisCreditsAlreadyExistsException.class)
    public ResponseEntity<UserResponse> handleUserWithThisCreditsAlreadyExistsException(UserWithThisCreditsAlreadyExistsException ex) {
        return switch (ex.getMessage()) {
            case "USER_WITH_THIS_EMAIL_ALREADY_EXISTS" -> new ResponseEntity<>(UserResponses.USER_WITH_THIS_EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
            case "USER_WITH_THIS_PHONE_ALREADY_EXISTS" -> new ResponseEntity<>(UserResponses.USER_WITH_THIS_PHONE_ALREADY_EXISTS, HttpStatus.CONFLICT);
            default -> new ResponseEntity<>(UserResponses.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        };
    }

    /**
     * Метод для обработки исключения MethodArgumentNotValidException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с ошибкой 422 Unprocessable Content.
     * */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_CONTENT)
    public UserResponse handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, List<String>> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.groupingBy(
                        FieldError::getField,
                        Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
                ));

        return new UserResponse("VALIDATION_ERROR", errors);
    }
}
