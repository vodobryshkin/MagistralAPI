package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
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
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;
import ru.rtkmagistral.magistralapi.service.spec.IMessageService;

import java.util.List;
import java.util.Optional;

/**
 * Сервиса для работы с функциональностью приложения, связанной с аутентификацией в приложении.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    @Value("${redis.resend_token.ttl}")
    private Long resendTokenTtl;

    private final IMessageService messageService;

    private final AuthenticationManager authenticationManager;

    private final IResendTokenRepository resendTokenRepository;
    private final UserRepository userRepository;

    /**
     * Метод для реализации логики входа в приложение.
     *
     * @param loginRequest запрос на вход в приложение.
     * @return результат входа в приложение.
     */
    @Override
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        if (authentication.isAuthenticated()) {
            List<String> authorities = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .toList();

            return new AuthResponse("OK", authorities);
        }

        throw new AuthException("INCORRECT_EMAIL_OR_PASSWORD");
    }

    /**
     * Метод для проверки на то, можно ли сейчас отправить новое письмо с подтверждением аккаунта.
     *
     * @param email адрес электронной почты пользователя.
     * @return сформированный респонс энтити.
     */
    @Override
    public ResendTokenDTO resend(String email) {
        Optional<ResendToken> resendTokenOptional = resendTokenRepository.findById(email);

        if (resendTokenOptional.isEmpty()) {
            User user = userRepository.findUserByEmail(email).orElseThrow(() -> new UserException("USER_NOT_FOUND"));

            ConfirmAccountMailRequest request = new ConfirmAccountMailRequest(
                    user.getName(),
                    user.getFathersName() == null? "": user.getFathersName(),
                    user.getEmail(),
                    "Подтверждение аккаунта"
            );

            messageService.sendConfirmAccountMessageToQueue(request);

            createResendToken(email);

            return new ResendTokenDTO(204);
        }

        ResendToken resendToken = resendTokenOptional.get();

        return new ResendTokenDTO(429, String.valueOf(resendToken.getTtlSeconds()));
    }

    /**
     * Метод для создания токена для контроля когда можно отправить новое письмо с подтверждением аккаунта.
     *
     * @param email адрес электронной почты пользователя.
     */
    @Override
    public void createResendToken(String email) {
        ResendToken resendToken = new ResendToken(email, resendTokenTtl);
        resendTokenRepository.save(resendToken);
    }
}
