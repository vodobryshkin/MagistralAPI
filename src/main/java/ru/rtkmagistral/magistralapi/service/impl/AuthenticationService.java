package ru.rtkmagistral.magistralapi.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.dto.auth.AuthResponse;
import ru.rtkmagistral.magistralapi.dto.auth.LoginRequest;
import ru.rtkmagistral.magistralapi.exception.AuthException;
import ru.rtkmagistral.magistralapi.service.spec.IAuthenticationService;

import java.util.List;

/**
 * Сервиса для работы с функциональностью приложения, связанной с аутентификацией в приложении.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationService implements IAuthenticationService {
    private final AuthenticationManager authenticationManager;

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
}
