package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.exception.ValidationResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForAuthenticatedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/users"
 */
@Tag(
        name = "Операции с пользователем",
        description = "Операции, связанные с пользователем (добавление нового пользователя, получение информации о пользователе для личного кабинета)."
)
@RestController
@RequestMapping(
        value = "/users",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/users".
     * Логика метода заключается в добавлении пользователя в систему.
     *
     * @param createUserRequest запрос на создание пользователя.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @Operation(
            summary = "Добавление нового пользователя в систему.",
            description = """
                    Добавление пользователя по переданным данным.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Пользователь был успешно создан.",
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
                    responseCode = "409",
                    description = "Пользователь с таким email/номером телефона уже существует.",
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
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "422",
                    description = "Переданные данные для регистрации пользователя семантически некорректные.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = ValidationResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Данные не соответствуют заявленным форматам, ввиду чего не прошли валидацию. Все возможные сообщения об ошибках валидации представлены в объекте-примере.",
                                            name = "VALIDATION_ERROR",
                                            value = """
                                                    {
                                                      "message": "VALIDATION_ERROR",
                                                      "validation_errors": {
                                                        "password": [
                                                          "MUST_CONTAIN_ONLY_ASCII_SYMBOLS",
                                                          "LENGTH_MUST_BE_BETWEEN_6_AND_32_SYMBOLS",
                                                          "CANNOT_BE_BLANK"
                                                        ],
                                                        "fathersName": [
                                                          "MUST_BE_PROPER_NOUN",
                                                          "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                        ],
                                                        "phone": [
                                                          "CANNOT_BE_BLANK",
                                                          "MUST_MATCH_FORMAT"
                                                        ],
                                                        "surname": [
                                                          "MUST_BE_PROPER_NOUN",
                                                          "CANNOT_BE_BLANK",
                                                          "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                        ],
                                                        "name": [
                                                          "MUST_BE_PROPER_NOUN",
                                                          "CANNOT_BE_BLANK",
                                                          "MUST_CONTAIN_ONLY_RUSSIAN_LETTERS"
                                                        ],
                                                        "agreeOnPersonalDataProcessing": [
                                                          "MUST_BE_TRUE"
                                                        ],
                                                        "email": [
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
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    description = "Данные, необходимые для создания пользователя.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateUserRequest.class)
                    )
            )
            @RequestBody @Valid CreateUserRequest createUserRequest
    ) {
        UserResponse userResponse = userService.createUser(createUserRequest);
        UserProfileDTO userProfileDTO = userService.getUserProfile(createUserRequest.getEmail());
        userResponse.setUserProfileDTO(userProfileDTO);

        String accessToken = jwtService.generateAccessToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");
        String refreshToken = jwtService.generateRefreshToken(createUserRequest.getEmail(), "ROLE_UNVERIFIED_USER");

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();

        authenticationService.createResendToken(createUserRequest.getEmail());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(userResponse);
    }

    @Operation(
            summary = "Получение данных о пользователе для личного кабинета.",
            description = """
                    По email'у, который зашифрован в access-токене из заголовка Authorization
                    получает необходимую для личного кабинета информацию о пользователе:
                    email, телефон, тип аккаунта пользователя (физ. лицо или юр. лицо), количество заказов, и информацию
                    о том, верифицирован ли пользователь или нет.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные о пользователе были успешно получены.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserProfileDTO.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Не удалось аутентифицировать пользователя.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Время жизни access-токена вышло.",
                                            name = "ACCESS_TOKEN_HAS_EXPIRED",
                                            value = "{\"message\":\"ACCESS_TOKEN_HAS_EXPIRED\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Access-токен семантически некорректен.",
                                            name = "ACCESS_TOKEN_INVALID",
                                            value = "{\"message\":\"ACCESS_TOKEN_INVALID\"}"
                                    ),
                                    @ExampleObject(
                                            description = "Не удалось найти пользователя с email из access-токена.",
                                            name = "USER_NOT_FOUND",
                                            value = "{\"message\":\"USER_NOT_FOUND\"}"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Недостаточно прав для того, чтобы выполнить операцию.",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Недостаточно прав для совершения операции",
                                            name = "INSUFFICIENT_RIGHTS",
                                            value = "{\"message\":\"INSUFFICIENT_RIGHTS\"}"
                                    )
                            }
                    )
            )
    })
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    @ForAuthenticatedUsers
    public ResponseEntity<UserProfileDTO> readUserProfile(Authentication authentication) {
        return ResponseEntity
                .ok()
                .body(userService.getUserProfile(authentication.getName()));
    }
}
