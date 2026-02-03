package ru.rtkmagistral.magistralapi.security.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MagistralSchemaUserDetailsService implements UserDetailsService {
    private final AuthenticationUserDAO authenticationUserDAO;

    /**
     * Метод для получения объекта UserDetails, опираясь на специфику схемы БД для данного приложения.
     *
     * @param email адрес электронной почты пользователя.
     * @return сформированный объект UserDetails.
     * @throws UsernameNotFoundException выкидывается если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        AuthenticationUserDTO user = authenticationUserDAO.findUserByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        UUID id = user.getUuid();

        List<String> roles = authenticationUserDAO.findUserRoles(id);
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        for (String role: roles) {
            authorities.add(new SimpleGrantedAuthority(role));
        }

        return User
                .withUsername(email)
                .password(new String(user.getPasswordHash(), StandardCharsets.UTF_8))
                .authorities(authorities)
                .build();
    }
}
