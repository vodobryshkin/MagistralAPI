package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.company.CompanyDTO;
import ru.rtkmagistral.magistralapi.dto.company.CreateCompanyRequest;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.exception.ValidationResponse;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/company"
 */
@Tag(
        name = "Операции с компаниями",
        description = "Операции, связанные с компаниями (добавление новой компании)."
)
@RestController
@RequestMapping(
        value = "/companies",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class CompanyController {
    private final IUserService userService;
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/company".
     * Логика метода заключается в добавлении пользователя и компании, которую он представляет, в систему.
     *
     * @param companyDTO запрос на создание пользователя и компании.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @Operation(
            summary = "Добавление новой компании в систему.",
            description = """
                    Добавление компании по переданным данным.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Компания была успешно создана.",
                    headers = {
                            @Header(
                                    name = "Authorization",
                                    description = "Access-токен с правами пользователя",
                                    schema = @Schema(type = "string")
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = "Cookie refresh_token с refresh-токеном с правами пользователя",
                                    schema = @Schema(type = "string")
                            )
                    },
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Компания c переданным ИНН не существует в сервисе Dadata.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Компании с таким ИНН не существует в Dadata.",
                                            name = "COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA",
                                            value = "{\"message\":\"COMPANY_WITH_THIS_INN_NOT_EXISTS_IN_DADATA\"}"
                                    ),
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Либо пользователь с таким email/номером телефона уже существует, либо компания с таким ИНН уже существует в Базе Данных приложения.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Пользователь с таким email уже существует.",
                                            name = "USER_WITH_THIS_EMAIL_ALREADY_EXISTS",
                                            value = "{\"message\":\"USER_WITH_THIS_EMAIL_ALREADY_EXISTS\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Пользователь с таким номером телефона уже существует.",
                                            name = "USER_WITH_THIS_PHONE_ALREADY_EXISTS",
                                            value = "{\"message\":\"USER_WITH_THIS_PHONE_ALREADY_EXISTS\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Компания с такими данными уже существует.",
                                            name = "COMPANY_ALREADY_EXISTS_IN_DATABASE",
                                            value = "{\"message\":\"COMPANY_ALREADY_EXISTS_IN_DATABASE\"}"
                                    ),
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Переданные данные для регистрации компании семантически некорректные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Компании с переданным ИНН не существует в Dadata.",
                                            name = "INN_NOT_MATCHES_WITH_DADATA",
                                            value = "{\"message\": \"INN_NOT_MATCHES_WITH_DADATA\"}"
                                    ),
                                    @ExampleObject(
                                            description = "КПП не соответствует данным из Dadata.",
                                            name = "KPP_NOT_MATCHES_WITH_DADATA",
                                            value = "{\"message\": \"KPP_NOT_MATCHES_WITH_DADATA\"}"
                                    ),
                                    @ExampleObject(
                                            description = "ОКВЭД не соответствует данным из Dadata.",
                                            name = "OKVED_NOT_MATCHES_WITH_DADATA",
                                            value = "{\"message\": \"OKVED_NOT_MATCHES_WITH_DADATA\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Название компании не соответствует данным из Dadata.",
                                            name = "TITLE_NOT_MATCHES_WITH_DADATA",
                                            value = "{\"message\": \"TITLE_NOT_MATCHES_WITH_DADATA\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Данные не соответствуют заявленным форматам, ввиду чего не прошли валидацию. Все возможные сообщения об ошибках валидации представлены в объекте-примере.",
                                            name = "VALIDATION_ERROR",
                                            value = """
                                                    {
                                                       "message": "VALIDATION_ERROR",
                                                       "validation_errors": {
                                                         "createUserRequest.password": [
                                                           "MUST_CONTAIN_ONLY_ASCII_SYMBOLS",
                                                           "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS",
                                                           "CANNOT_BE_BLANK"
                                                         ],
                                                         "createUserRequest.email": [
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_MATCH_FORMAT"
                                                         ],
                                                         "createCompanyRequest.okved": [
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_MATCH_FORMAT"
                                                         ],
                                                         "createUserRequest.surname": [
                                                           "MUST_BE_PROPER_NOUN",
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                         ],
                                                         "createUserRequest.fathersName": [
                                                           "MUST_BE_PROPER_NOUN",
                                                           "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                         ],
                                                         "createUserRequest.phone": [
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_MATCH_FORMAT"
                                                         ],
                                                         "createUserRequest.name": [
                                                           "MUST_BE_PROPER_NOUN",
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                         ],
                                                         "createCompanyRequest.title": [
                                                           "CANNOT_BE_BLANK"
                                                         ],
                                                         "createCompanyRequest.kpp": [
                                                           "MUST_MATCH_FORMAT",
                                                           "CANNOT_BE_BLANK"
                                                         ],
                                                         "createCompanyRequest.agreeToTheProcessingOfCourierServices": [
                                                           "MUST_BE_TRUE"
                                                         ],
                                                         "createUserRequest.agreeOnPersonalDataProcessing": [
                                                           "MUST_BE_TRUE"
                                                         ],
                                                         "createCompanyRequest.inn": [
                                                           "CANNOT_BE_BLANK",
                                                           "MUST_MATCH_FORMAT"
                                                         ]
                                                       }
                                                     }
                                                    """
                                    )
                            }
                    )
            )
    })
    @PostMapping
    public ResponseEntity<UserResponse> createCompany(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, необходимые для создания компании.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CompanyDTO.class)
                    )
            )
            @RequestBody @Valid CompanyDTO companyDTO
    ) {
        CreateUserRequest createUserRequest = companyDTO.getCreateUserRequest();
        CreateCompanyRequest companyRequest = companyDTO.getCreateCompanyRequest();

        UserResponse userResponse = userService.createLegalUser(createUserRequest, companyRequest);
        UserProfileDTO userProfileDTO = userService.getUserProfile(createUserRequest.getEmail());
        userResponse.setUserProfileDTO(userProfileDTO);

        String accessToken = jwtService.generateAccessToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");
        String refreshToken = jwtService.generateRefreshToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        authenticationService.createResendToken(createUserRequest.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(userResponse);
    }
}
