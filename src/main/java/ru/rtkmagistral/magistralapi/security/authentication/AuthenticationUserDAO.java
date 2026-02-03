package ru.rtkmagistral.magistralapi.security.authentication;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * DAO, которое нужно для получения необходимых данных о пользователе для возможности его аутентификации.
 * Под капотом используется JDBC для быстроты запросов.
 */
@Repository
@RequiredArgsConstructor
public class AuthenticationUserDAO {
    private final JdbcClient jdbcClient;

    /**
     * Метод для получения всех необходимых данных для аутентификации пользователя по его адресу электронной почты.
     *
     * @param email адрес электронной почты пользователя.
     * @return null, если пользователя с таким email нет, или DTO со всей необходимой для аутентификации информацией.
     */
    public Optional<AuthenticationUserDTO> findUserByEmail(String email) {
        return jdbcClient.sql("SELECT id, email, password_hash FROM users WHERE email = :email;")
                .param("email", email)
                .query((rs, n) -> new AuthenticationUserDTO(
                        UUID.fromString(rs.getString("id")),
                        rs.getString("email"),
                        rs.getBytes("password_hash")
                ))
                .optional();
    }

    /**
     * Метод для получения ролей пользователя по его id.
     *
     * @param id id пользователя.
     * @return список ролей пользователя.
     */
    public List<String> findUserRoles(UUID id) {
        return jdbcClient.sql("SELECT role FROM roles WHERE user_id = :id;")
                .param("id", id)
                .query(String.class)
                .list();
    }
}
