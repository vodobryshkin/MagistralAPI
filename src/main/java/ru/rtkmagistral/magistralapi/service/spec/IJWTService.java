package ru.rtkmagistral.magistralapi.service.spec;

import java.util.List;

/**
 * Интерфейс для определения функциональности JWTService для работы с JWT-токенами
 */
public interface IJWTService {
    /**
     * Метод для генерации access-токена.
     *
     * @param username имя пользователя, к которому создаётся access-токен.
     * @param role роль пользователя, к которому создаётся access-токен.
     * @return сгенерированный access-токен.
     */
    String generateAccessToken(String username, String role);

    /**
     * Метод для генерации access-токена.
     *
     * @param username имя пользователя, к которому создаётся access-токен.
     * @param roles роли пользователя, к которому создаётся access-токен.
     * @return сгенерированный access-токен.
     */
    String generateAccessToken(String username, List<String> roles);

    /**
     * Метод для генерации refresh-токена.
     *
     * @param username имя пользователя, к которому создаётся refresh-токен.
     * @param role роль пользователя, к которому создаётся refresh-токен.
     * @return сгенерированный refresh-токен.
     */
    String generateRefreshToken(String username, String role);

    /**
     * Метод для генерации refresh-токена.
     *
     * @param username имя пользователя, к которому создаётся refresh-токен.
     * @param roles роли пользователя, к которому создаётся refresh-токен.
     * @return сгенерированный refresh-токен.
     */
    String generateRefreshToken(String username, List<String> roles);

    /**
     * Метод для проверки корректности токена.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    boolean isTokenValid(String token);

    /**
     * Метод для получения имени пользователя в токене.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    String extractUsername(String token);

    /**
     * Метод для получения ролей в токене.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    List<String> extractRoles(String token);
}
