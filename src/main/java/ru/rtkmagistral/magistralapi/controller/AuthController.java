package ru.rtkmagistral.magistralapi.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;
import ru.rtkmagistral.magistralapi.exception.AuthException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;

import java.util.List;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/auth"
 */

@Tag(
        name = "Аутентификация",
        description = "Операции, связанные с получением токенов для аутентификации пользователя (вход в аккаунт, " +
                "получение нового access-токена после истечения срока годности предыдущего)"
)
@RestController
@RequestMapping(
        value = "/auth",
        produces = MediaType.APPLICATION_JSON_VALUE
)
@RequiredArgsConstructor
public class AuthController {
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;
    private final IUserService userService;

    @Operation(
            summary = "Вход в аккаунт",
            description = """
                    Возвращает access-токен для идентификации текущего пользователя в заголовке Authorization.
                    Устанавливает Cookie refresh_token с refresh-токеном для обновления access-токена.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Вход прошёл успешно"),
            @ApiResponse(responseCode = "403", description = "Неверный логин или пароль")
    })
    @PostMapping(
            value = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid LoginRequest loginRequest) {
        AuthResponse authResponse = authenticationService.login(loginRequest);
        List<String> authorities = authResponse.getAuthorities();

        String accessToken = jwtService.generateAccessToken(loginRequest.getEmail(), authorities);
        String refreshToken = jwtService.generateRefreshToken(loginRequest.getEmail(), authorities);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }

    @Operation(
            summary = "Получение нового access-токена",
            description = """
                    Возвращает новый access-токен для идентификации текущего пользователя в заголовке Authorization.
                    Необходим установленный Cookie refresh_token с refresh-токеном для обновления access-токена, иначе метод выполнится некорректно.
                    """
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Новый access-токен получен успешно"),
            @ApiResponse(responseCode = "401", description = "Cookie refresh_token невалиден/неустановлен/просрочен")
    })
    @GetMapping(value = "/refresh")
    public ResponseEntity<?> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            throw new AuthException("REFRESH_TOKEN_EXPIRED");
        }

        String email = jwtService.extractUsername(refreshToken);

        if (!userService.checkUserExists(email)) {
            throw new AuthException("CANNOT_IDENTIFY_USER_USING_THIS_REFRESH_TOKEN");
        }

        List<String> roles = jwtService.extractRoles(refreshToken);

        String accessToken = jwtService.generateAccessToken(email, roles);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();
    }
}
