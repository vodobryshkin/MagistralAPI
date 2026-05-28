package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.exception.AuthException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
class AuthControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IAuthenticationService authenticationService;

    @MockitoBean
    IJWTService jwtService;

    @MockitoBean
    IUserService userService;

    @Test
    @DisplayName("POST /auth/login возвращает 200 и проставляет Authorization + Set-Cookie")
    void login_success_returns200WithTokens() throws Exception {
        when(authenticationService.login(any()))
                .thenReturn(new AuthResponse("OK", List.of("ROLE_VERIFIED_USER")));
        when(userService.getUserProfile("vova@example.com"))
                .thenReturn(new UserProfileDTO("vova@example.com", "+79614667210", null, 0L, true));
        when(jwtService.generateAccessToken(any(), any(List.class)))
                .thenReturn("Bearer access-token");
        when(jwtService.generateRefreshToken(any(), any(List.class)))
                .thenReturn("refresh-token");

        String json = """
            {"email": "vova@example.com", "password": "secret123"}
        """;

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-token"))
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));
    }

    @Test
    @DisplayName("POST /auth/login возвращает 401 при ошибке INCORRECT_EMAIL_OR_PASSWORD")
    void login_incorrectCredentials_returns401() throws Exception {
        when(authenticationService.login(any()))
                .thenThrow(new AuthException("INCORRECT_EMAIL_OR_PASSWORD"));

        String json = """
            {"email": "vova@example.com", "password": "wrong"}
        """;

        mvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/refresh без cookie возвращает 401 REFRESH_TOKEN_INVALID")
    void refresh_noCookie_returns401() throws Exception {
        mvc.perform(get("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/refresh с просроченным токеном возвращает 401")
    void refresh_invalidToken_returns401() throws Exception {
        when(jwtService.isTokenValid("bad-token")).thenReturn(false);

        mvc.perform(get("/api/v1/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refresh_token", "bad-token")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /auth/refresh с валидным токеном, но пользователь не найден — 404 CANNOT_IDENTIFY_USER...")
    void refresh_validTokenUserMissing_returns404() throws Exception {
        when(jwtService.isTokenValid("ok-token")).thenReturn(true);
        when(jwtService.extractUsername("ok-token")).thenReturn("nope@example.com");
        when(userService.checkUserExists("nope@example.com")).thenReturn(false);

        mvc.perform(get("/api/v1/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refresh_token", "ok-token")))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /auth/refresh с валидным токеном и существующим пользователем возвращает 204 и Authorization")
    void refresh_success_returns204WithAuthorization() throws Exception {
        when(jwtService.isTokenValid("ok-token")).thenReturn(true);
        when(jwtService.extractUsername("ok-token")).thenReturn("vova@example.com");
        when(userService.checkUserExists("vova@example.com")).thenReturn(true);
        when(jwtService.extractRoles("ok-token")).thenReturn(List.of("ROLE_VERIFIED_USER"));
        when(jwtService.generateAccessToken(any(), any(List.class)))
                .thenReturn("Bearer new-access");

        mvc.perform(get("/api/v1/auth/refresh").cookie(new jakarta.servlet.http.Cookie("refresh_token", "ok-token")))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer new-access"));
    }
}
