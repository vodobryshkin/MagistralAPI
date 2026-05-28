package ru.rtkmagistral.magistralapi.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.rtkmagistral.magistralapi.dto.resend_token.ResendTokenDTO;
import ru.rtkmagistral.magistralapi.dto.token.VerifyResponse;
import ru.rtkmagistral.magistralapi.dto.user.UserProfileDTO;
import ru.rtkmagistral.magistralapi.exception.AppExceptionHandler;
import ru.rtkmagistral.magistralapi.exception.ConfirmationLinkException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IConfirmationLinkService;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;
import ru.rtkmagistral.magistralapi.service.spec.IUserService;
import ru.rtkmagistral.magistralapi.support.WebTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailVerificationController.class)
@Import({AppExceptionHandler.class, WebTestSupport.class})
class EmailVerificationControllerIT {

    @Autowired
    MockMvc mvc;

    @MockitoBean
    IAuthenticationService authenticationService;
    @MockitoBean
    IConfirmationLinkService confirmationLinkService;
    @MockitoBean
    IUserService userService;
    @MockitoBean
    IJWTService jwtService;

    @Test
    @DisplayName("POST /confirmation-links — успешно отправлено: 204")
    void resend_success_returns204() throws Exception {
        when(authenticationService.resend("vova@example.com"))
                .thenReturn(new ResendTokenDTO(204));

        mvc.perform(post("/api/v1/confirmation-links")
                        .with(user("vova@example.com").roles("UNVERIFIED_USER")))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("POST /confirmation-links — слишком рано: 429 и Retry-After")
    void resend_tooSoon_returns429WithRetryAfter() throws Exception {
        when(authenticationService.resend("vova@example.com"))
                .thenReturn(new ResendTokenDTO(429, "42"));

        mvc.perform(post("/api/v1/confirmation-links")
                        .with(user("vova@example.com").roles("UNVERIFIED_USER")))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "42"));
    }

    @Test
    @DisplayName("PUT /confirmation-links/{id} — валидный UUID и активная ссылка — 200")
    void verify_validLink_returns200WithAuthorization() throws Exception {
        String id = "fff04aa8-3458-49a2-ac2d-b1560534c085";

        VerifyResponse verifyResponse = new VerifyResponse(true, "vova@example.com");
        when(confirmationLinkService.verifyConfirmationLink(id)).thenReturn(verifyResponse);
        when(userService.verifyUser("vova@example.com"))
                .thenReturn(new UserProfileDTO("vova@example.com", "+79614667210", null, 0L, true));
        when(jwtService.generateAccessToken(any(), any(String.class))).thenReturn("Bearer access-new");
        when(jwtService.generateRefreshToken(any(), any(String.class))).thenReturn("refresh-new");

        mvc.perform(put("/api/v1/confirmation-links/{id}", id))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.AUTHORIZATION, "Bearer access-new"))
                .andExpect(jsonPath("$.status").value(true))
                .andExpect(jsonPath("$.user.email").value("vova@example.com"));
    }

    @Test
    @DisplayName("PUT /confirmation-links/{id} — невалидная ссылка — 404")
    void verify_invalidLink_returns404() throws Exception {
        String id = "fff04aa8-3458-49a2-ac2d-b1560534c085";
        when(confirmationLinkService.verifyConfirmationLink(id))
                .thenThrow(new ConfirmationLinkException("CONFIRMATION_LINK_HAS_EXPIRED_OR_INVALID"));

        mvc.perform(put("/api/v1/confirmation-links/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /confirmation-links/{id} — некорректный UUID — 400")
    void verify_invalidUuid_returns400() throws Exception {
        mvc.perform(put("/api/v1/confirmation-links/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest());
    }
}
