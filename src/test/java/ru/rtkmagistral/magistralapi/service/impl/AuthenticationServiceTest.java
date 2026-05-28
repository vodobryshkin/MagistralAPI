package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import ru.rtkmagistral.magistralapi.domain.jpa.User;
import ru.rtkmagistral.magistralapi.domain.redis.ResendToken;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;
import ru.rtkmagistral.magistralapi.dto.mail.ConfirmAccountMailRequest;
import ru.rtkmagistral.magistralapi.dto.resend_token.ResendTokenDTO;
import ru.rtkmagistral.magistralapi.exception.AuthException;
import ru.rtkmagistral.magistralapi.exception.UserException;
import ru.rtkmagistral.magistralapi.repository.IResendTokenRepository;
import ru.rtkmagistral.magistralapi.repository.UserRepository;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    IMessageService messageService;
    @Mock
    IResendTokenRepository resendTokenRepository;
    @Mock
    UserRepository userRepository;

    @InjectMocks
    AuthenticationService authenticationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authenticationService, "resendTokenTtl", 60L);
    }

    @Test
    @DisplayName("login возвращает OK и список ролей при удачной аутентификации")
    void login_success_returnsOkAndAuthorities() {
        LoginRequest req = new LoginRequest("vova@example.com", "secret");
        Authentication authResult = UsernamePasswordAuthenticationToken.authenticated(
                "vova@example.com",
                "secret",
                List.of(new SimpleGrantedAuthority("ROLE_VERIFIED_USER"))
        );
        when(authenticationManager.authenticate(any())).thenReturn(authResult);

        AuthResponse response = authenticationService.login(req);

        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getAuthorities()).containsExactly("ROLE_VERIFIED_USER");
    }

    @Test
    @DisplayName("login кидает INCORRECT_EMAIL_OR_PASSWORD если authentication.isAuthenticated() == false")
    void login_notAuthenticated_throws() {
        LoginRequest req = new LoginRequest("vova@example.com", "secret");
        Authentication authResult = new UsernamePasswordAuthenticationToken("vova@example.com", "secret");
        when(authenticationManager.authenticate(any())).thenReturn(authResult);

        assertThatThrownBy(() -> authenticationService.login(req))
                .isInstanceOf(AuthException.class)
                .hasMessage("INCORRECT_EMAIL_OR_PASSWORD");
    }

    @Test
    @DisplayName("resend публикует письмо и создаёт токен, если резенд-токена ещё не существует")
    void resend_noToken_sendsMessageAndCreatesToken() {
        when(resendTokenRepository.findById("vova@example.com")).thenReturn(Optional.empty());

        User user = new User("Владимир", "Добрышкин", "Александрович",
                "vova@example.com", "+79614667210",
                "h".getBytes(StandardCharsets.UTF_8));
        when(userRepository.findUserByEmail("vova@example.com")).thenReturn(Optional.of(user));

        ResendTokenDTO dto = authenticationService.resend("vova@example.com");

        assertThat(dto.getCode()).isEqualTo(204);

        ArgumentCaptor<ConfirmAccountMailRequest> mailCaptor =
                ArgumentCaptor.forClass(ConfirmAccountMailRequest.class);
        verify(messageService).sendConfirmAccountMessageToQueue(mailCaptor.capture());
        assertThat(mailCaptor.getValue().getTo()).isEqualTo("vova@example.com");

        ArgumentCaptor<ResendToken> tokenCaptor = ArgumentCaptor.forClass(ResendToken.class);
        verify(resendTokenRepository).save(tokenCaptor.capture());
        assertThat(tokenCaptor.getValue().getEmail()).isEqualTo("vova@example.com");
        assertThat(tokenCaptor.getValue().getTtlSeconds()).isEqualTo(60L);
    }

    @Test
    @DisplayName("resend возвращает 429 и оставшееся время если токен ещё активен")
    void resend_tokenAlive_returnsRetryAfter() {
        ResendToken existing = new ResendToken("vova@example.com", 42L);
        when(resendTokenRepository.findById("vova@example.com")).thenReturn(Optional.of(existing));

        ResendTokenDTO dto = authenticationService.resend("vova@example.com");

        assertThat(dto.getCode()).isEqualTo(429);
        assertThat(dto.getMessage()).isEqualTo("42");

        verify(messageService, never()).sendConfirmAccountMessageToQueue(any());
        verify(resendTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("resend для несуществующего пользователя кидает USER_NOT_FOUND")
    void resend_userNotFound_throws() {
        when(resendTokenRepository.findById("nope@example.com")).thenReturn(Optional.empty());
        when(userRepository.findUserByEmail("nope@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationService.resend("nope@example.com"))
                .isInstanceOf(UserException.class)
                .hasMessage("USER_NOT_FOUND");
    }

    @Test
    @DisplayName("createResendToken сохраняет ResendToken с заданным TTL")
    void createResendToken_savesWithConfiguredTtl() {
        authenticationService.createResendToken("vova@example.com");

        ArgumentCaptor<ResendToken> captor = ArgumentCaptor.forClass(ResendToken.class);
        verify(resendTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getEmail()).isEqualTo("vova@example.com");
        assertThat(captor.getValue().getTtlSeconds()).isEqualTo(60L);
    }
}
