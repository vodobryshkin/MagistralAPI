package ru.rtkmagistral.magistralapi.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;

import java.util.List;

/**
 * Контроллер, принимающий запросы идущие на эндпойнт "/auth"
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final IJWTService jwtService;
    private final IAuthenticationService authenticationService;

    @PostMapping("/login")
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
                .status(HttpStatus.CREATED)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(authResponse);
    }

    @GetMapping("/refresh")
    public ResponseEntity<Void> refresh(@CookieValue(name = "refresh_token", required = false) String refreshToken) {
        if (refreshToken == null || !jwtService.isTokenValid(refreshToken)) {
            return ResponseEntity.status(401).build();
        }

        String email = jwtService.extractUsername(refreshToken);
        List<String> roles = jwtService.extractRoles(refreshToken);

        String accessToken = jwtService.generateAccessToken(email, roles);

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .header(HttpHeaders.AUTHORIZATION, accessToken)
                .build();
    }
}
