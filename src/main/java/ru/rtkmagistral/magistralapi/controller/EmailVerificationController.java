package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.resend_token.ResendTokenDTO;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.security.authorization.ForUnverifiedUsers;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.validation.formats.uuid.UUID;

@RestController
@RequestMapping("/confirmation-links")
@RequiredArgsConstructor
public class EmailVerificationController {
    private final IAuthenticationService authenticationService;
    private final IConfirmationLinkService confirmationLinkService;
    private final IUserService userService;
    private final IJWTService jwtService;

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
    @PutMapping("/{id}")
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
