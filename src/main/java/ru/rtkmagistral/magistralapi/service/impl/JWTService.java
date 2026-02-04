package ru.rtkmagistral.magistralapi.service.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JWTService implements IJWTService {
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh.expiration}")
    private long refreshTokenExpiration;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Метод для генерации access-токена.
     *
     * @param username имя пользователя, к которому создаётся access-токен.
     * @param role роль пользователя, к которому создаётся access-токен.
     * @return сгенерированный access-токен.
     */
    @Override
    public String generateAccessToken(String username, String role) {
        return generateAccessToken(username, List.of(role));
    }

    /**
     * Метод для генерации access-токена.
     *
     * @param username имя пользователя, к которому создаётся access-токен.
     * @param roles роли пользователя, к которому создаётся access-токен.
     * @return сгенерированный access-токен.
     */
    @Override
    public String generateAccessToken(String username, List<String> roles) {
        return "Bearer " + generateToken(username, roles, accessTokenExpiration);
    }

    /**
     * Метод для генерации refresh-токена.
     *
     * @param username имя пользователя, к которому создаётся refresh-токен.
     * @param role роль пользователя, к которому создаётся refresh-токен.
     * @return сгенерированный refresh-токен.
     */
    @Override
    public String generateRefreshToken(String username, String role) {
        return generateRefreshToken(username, List.of(role));
    }

    /**
     * Метод для генерации refresh-токена.
     *
     * @param username имя пользователя, к которому создаётся refresh-токен.
     * @param roles роли пользователя, к которому создаётся refresh-токен.
     * @return сгенерированный refresh-токен.
     */
    @Override
    public String generateRefreshToken(String username, List<String> roles) {
        return generateToken(username, roles, refreshTokenExpiration);
    }

    private String generateToken(String username, List<String> roles, long expiration) {
        Date now = new Date();

        return Jwts.builder()
                .claims(Map.of("roles", roles))
                .subject(username)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiration))
                .signWith(secretKey)
                .compact();
    }

    /**
     * Метод для проверки корректности токена.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    @Override
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Метод для получения имени пользователя в токене.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    @Override
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }

    /**
     * Метод для получения ролей в токене.
     *
     * @param token токен, который необходимо проверить.
     * @return результат проверки токена.
     */
    @Override
    public List<String> extractRoles(String token) {
        Claims claims = extractAllClaims(token);

        Object raw = claims.get("roles");
        if (raw == null) {
            return List.of();
        }

        if (raw instanceof List<?> list) {
            return list.stream()
                    .map(String::valueOf)
                    .toList();
        }

        String s = String.valueOf(raw).trim();
        if (s.isEmpty()) {
            return List.of();
        }

        return List.of(s.split("\\s*,\\s*"));
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
