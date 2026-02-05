package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.dto.user.CreateUserRequest;
import ru.rtkmagistral.magistralapi.dto.user.UserResponse;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.validation.formats.uuid.UUID;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/users"
 */
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final IUserService userService;
    private final IConfirmationLinkService confirmationLinkService;
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;

    /**
     * Метод, принимающий POST-запросы идущие на эндпойнт "/users".
     * Логика метода заключается в добавлении пользователя в систему.
     *
     * @param createUserRequest запрос на создание пользователя.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<UserResponse> createUser(@RequestBody @Valid CreateUserRequest createUserRequest) {
        UserResponse userResponse = userService.createUser(createUserRequest);

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
                .ok()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(userResponse);
    }

    /**
     * Метод, принимающий PATCH-запросы идущие на эндпойнт "/users/{id}".
     * Логика метода заключается в подтверждении пользователя в системе.
     *
     * @param id ссылки на подтверждение.
     * @return ответ на запрос с информацией о прошедшей операции.
     */
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<VerifyResponse> verifyUser(@PathVariable @Valid @UUID String id) {
        VerifyResponse verifyResponse = confirmationLinkService.verifyConfirmationLink(id);
        String email = verifyResponse.getMessage();

        userService.verifyUser(email);
        verifyResponse.setMessage(null);

        String accessToken = jwtService.generateAccessToken(email, "ROLE_VERIFIED_USER");
        String refreshToken = jwtService.generateRefreshToken(email, "ROLE_VERIFIED_USER");

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .sameSite("Strict")
                .path("/")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(verifyResponse);
    }

}
