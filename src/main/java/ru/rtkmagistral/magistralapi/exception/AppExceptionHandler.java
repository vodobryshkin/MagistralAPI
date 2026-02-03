package ru.rtkmagistral.magistralapi.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import ru.rtkmagistral.magistralapi.dto.company.CompanyResponse;
import ru.rtkmagistral.magistralapi.dto.company.CompanyResponses;
import ru.rtkmagistral.magistralapi.dto.confirmation_link.ConfirmationLinkResponses;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
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
     * Метод для обработки исключения UserException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
     */
    @ExceptionHandler(UserException.class)
    public ResponseEntity<UserResponse> handleUserWithThisCreditsAlreadyExistsException(UserException ex) {
        return switch (ex.getMessage()) {
            case "USER_WITH_THIS_EMAIL_ALREADY_EXISTS" -> new ResponseEntity<>(UserResponses.USER_WITH_THIS_EMAIL_ALREADY_EXISTS, HttpStatus.CONFLICT);
            case "USER_WITH_THIS_PHONE_ALREADY_EXISTS" -> new ResponseEntity<>(UserResponses.USER_WITH_THIS_PHONE_ALREADY_EXISTS, HttpStatus.CONFLICT);
            default -> new ResponseEntity<>(UserResponses.BAD_REQUEST, HttpStatus.BAD_REQUEST);
        };
    }

    /**
     * Метод для обработки исключения CompanyException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
     */
    @ExceptionHandler(CompanyException.class)
    public ResponseEntity<CompanyResponse> handleCompanyCreditsException(CompanyException ex) {
        return switch (ex.getMessage()) {
            case "COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA" -> new ResponseEntity<>(CompanyResponses.COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA, HttpStatus.UNPROCESSABLE_CONTENT);
            case "INN_NOT_MATCHES_WITH_DADATA" -> new ResponseEntity<>(CompanyResponses.INN_NOT_MATCHES_WITH_DADATA, HttpStatus.UNPROCESSABLE_CONTENT);
            case "KPP_NOT_MATCHES_WITH_DADATA" -> new ResponseEntity<>(CompanyResponses.KPP_NOT_MATCHES_WITH_DADATA, HttpStatus.UNPROCESSABLE_CONTENT);
            case "OKVED_NOT_MATCHES_WITH_DADATA" -> new ResponseEntity<>(CompanyResponses.OKVED_NOT_MATCHES_WITH_DADATA, HttpStatus.UNPROCESSABLE_CONTENT);
            case "TITLE_NOT_MATCHES_WITH_DADATA" -> new ResponseEntity<>(CompanyResponses.TITLE_NOT_MATCHES_WITH_DADATA, HttpStatus.UNPROCESSABLE_CONTENT);
            case "COMPANY_ALREADY_EXISTS_IN_DATABASE" -> new ResponseEntity<>(CompanyResponses.COMPANY_ALREADY_EXISTS_IN_DATABASE, HttpStatus.CONFLICT);
            default -> new ResponseEntity<>(CompanyResponses.PROBLEMS_WHILE_ADDING_A_COMPANY, HttpStatus.BAD_REQUEST);
        };
    }

    /**
     * Метод для обработки исключения DadataClientException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с сообщением в зависимости от типа исключения.
     */
    @ExceptionHandler(DadataClientException.class)
    public ResponseEntity<CompanyResponse> handleDadataClientException(DadataClientException ex) {
        return switch (ex.getMessage()) {
            case "EMPTY_REQUEST_FOR_DADATA" -> new ResponseEntity<>(CompanyResponses.EMPTY_REQUEST_FOR_DADATA, HttpStatus.BAD_REQUEST);
            case "CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN" -> new ResponseEntity<>(CompanyResponses.CANT_FIND_DATA_IN_DADATA_FOR_CURRENT_INN, HttpStatus.NOT_FOUND);
            case "DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED" -> new ResponseEntity<>(CompanyResponses.DADATA_ERROR_WHILE_PARSING_INN_KPP_OKVED, HttpStatus.UNPROCESSABLE_CONTENT);
            default -> new ResponseEntity<>(new CompanyResponse(ex.getMessage(), null), HttpStatus.BAD_REQUEST);
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

    /**
     * Метод для обработки исключения ConfirmationLinkException.
     *
     * @param ex ошибка, которую вернул метод.
     * @return HTTP-ответ с ошибкой 404 Not found.
     * */
    @ExceptionHandler(ConfirmationLinkException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public VerifyResponse handleValidationException(ConfirmationLinkException ex) {
        return ConfirmationLinkResponses.CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID;
    }
}
