package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.resend_token.ResendTokenDTO;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.security.authorization.ForUnverifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.validation.formats.uuid.UUID;

@Tag(
        name = "Подтверждение почты",
        description = """
                Операции, связанные с подтверждением почты пользователя (проверка токена на корректность,
                запрос нового письма с токеном).
                """
)
@RestController
@RequestMapping(
        value = "/confirmation-links",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class EmailVerificationController {
    private final IAuthenticationService authenticationService;
    private final IConfirmationLinkService confirmationLinkService;
    private final IUserService userService;
    private final IJWTService jwtService;

    @Operation(
            summary = "Запрос повторной отправки письма с токеном на подтверждение аккаунта.",
            description = """
                    Проверяет возможность повторной отправки письма с токеном на подтверждение аккаунта.
                    Если письмо отправить нельзя (ввиду того, что после отправки письма не прошло достаточно времени),
                    то присылает ответ с заголовком Retry-After с количеством секунд, через которое можно будет предпринять
                    повторную попытку отправки письма с токеном на подтверждение аккаунта.
                    Если письмо можно отправить, то отправляет его.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Письмо с токеном было успешно отправлено на почту пользователя."
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
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Письмо с токеном не было отправлено на почту пользователя из-за того, что прошло недостаточно времени с предыдущей отправки.",
                    headers = {
                            @Header (
                                    name = "Retry-After",
                                    description = "Количество секунд, через которое запрос выполнится успешно",
                                    schema = @Schema(type = "number")
                            )
                    }
            ),
    })
    @PostMapping
    @ForUnverifiedUsers
    public ResponseEntity<Void> resend(Authentication authentication) {
        ResendTokenDTO resendTokenDTO = authenticationService.resend(authentication.getName());

        if (resendTokenDTO.getCode() == 429) {
            return ResponseEntity.status(429)
                    .header("Retry-After", resendTokenDTO.getMessage())
                    .build();
        }

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .build();
    }

    /**
     * Метод, принимающий PATCH-запросы идущие на эндпойнт "/confirmation-links/{id}".
     * Логика метода заключается в подтверждении пользователя в системе.
     *
     * @param id ссылки на подтверждение.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @Operation(
            summary = "Проверка валидности переданного токена для верификации пользователя.",
            description = """
                    Проверяет токен на валидность (на семантическую корректность, время жизни).
                    Если проверка прошла успешно, то возвращает access-токен для идентификации текущего пользователя
                    с обновлёнными правами в заголовке Authorization.
                    Устанавливает Cookie refresh_token с refresh-токеном с обновлёнными правами для обновления access-токена.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Верификация прошла успешно",
                    headers = {
                            @Header(
                                    name = "Authorization",
                                    description = "Access-токен с обновлёнными правами пользователя",
                                    schema = @Schema(type = "string")
                            ),
                            @Header(
                                    name = "Set-Cookie",
                                    description = "Cookie refresh_token с refresh-токеном с обновлёнными правами пользователя",
                                    schema = @Schema(type = "string")
                            )
                    },
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VerifyResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Операция завершилась успешно",
                                            name = "OK",
                                            value = "{\"status\": true}"
                                    ),
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Переданный на подтверждение токен не был найден в системе (ввиду невалидности или истечения времени жизни)",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = VerifyResponse.class),
                            examples = {
                                    @ExampleObject(
                                            description = "Операция завершилась неудачно",
                                            name = "CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID",
                                            value = "{\"status\": false, \"message\": \"CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID\"}"
                                    ),
                            }
                    )
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<VerifyResponse> verifyUser(
            @Parameter(
                    name = "id",
                    description = "UUID-токен, идентифицирующий ссылку на подтверждение. Должен быть передан как часть пути.",
                    schema = @Schema(type = "string"),
                    example = "fff04aa8-3458-49a2-ac2d-b1560534c085"
            )
            @PathVariable @Valid @UUID String id
    ) {
        VerifyResponse verifyResponse = confirmationLinkService.verifyConfirmationLink(id);
        String email = verifyResponse.getMessage();

        UserProfileDTO userProfileDTO = userService.verifyUser(email);
        verifyResponse.setMessage(null);
        verifyResponse.setUserProfileDTO(userProfileDTO);

        String accessToken = jwtService.generateAccessToken(email, "ROLE_VERIFIED_USER");
        String refreshToken = jwtService.generateRefreshToken(email, "ROLE_VERIFIED_USER");

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(verifyResponse);
    }
}
