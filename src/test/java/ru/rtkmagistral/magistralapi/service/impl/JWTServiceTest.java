package ru.rtkmagistral.magistralapi.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class JWTServiceTest {

    private JWTService jwtService;
    private static final String SECRET =
            "ThisIsATestSecretKeyForJwtServiceMustBeAtLeast32BytesLong!!!";

    @BeforeEach
    void setUp() {
        jwtService = new JWTService();
        ReflectionTestUtils.setField(jwtService, "secret", SECRET);
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(jwtService, "refreshTokenExpiration", 3_600_000L);
        jwtService.init();
    }

    @Test
    @DisplayName("generateAccessToken возвращает токен с префиксом Bearer")
    void generateAccessToken_hasBearerPrefix() {
        String token = jwtService.generateAccessToken("vova@example.com", "ROLE_UNVERIFIED_USER");

        assertThat(token).startsWith("Bearer ");
        String raw = token.substring("Bearer ".length());
        assertThat(jwtService.isTokenValid(raw)).isTrue();
        assertThat(jwtService.extractUsername(raw)).isEqualTo("vova@example.com");
        assertThat(jwtService.extractRoles(raw)).containsExactly("ROLE_UNVERIFIED_USER");
    }

    @Test
    @DisplayName("generateRefreshToken возвращает токен без префикса Bearer")
    void generateRefreshToken_noBearerPrefix() {
        String token = jwtService.generateRefreshToken("vova@example.com", "ROLE_VERIFIED_USER");

        assertThat(token).doesNotStartWith("Bearer ");
        assertThat(jwtService.isTokenValid(token)).isTrue();
        assertThat(jwtService.extractUsername(token)).isEqualTo("vova@example.com");
    }

    @Test
    @DisplayName("Сгенерированный токен содержит все переданные роли (список)")
    void generateAccessToken_multipleRoles() {
        String raw = jwtService.generateAccessToken(
                "vova@example.com",
                List.of("ROLE_VERIFIED_USER", "ROLE_MODERATOR")
        ).substring("Bearer ".length());

        assertThat(jwtService.extractRoles(raw))
                .containsExactlyInAnyOrder("ROLE_VERIFIED_USER", "ROLE_MODERATOR");
    }

    @Test
    @DisplayName("isTokenValid возвращает false для бессмысленной строки")
    void isTokenValid_garbage_returnsFalse() {
        assertThat(jwtService.isTokenValid("not.a.jwt")).isFalse();
        assertThat(jwtService.isTokenValid("")).isFalse();
    }

    @Test
    @DisplayName("isTokenValid возвращает false для просроченного токена")
    void isTokenValid_expiredToken_returnsFalse() {
        ReflectionTestUtils.setField(jwtService, "accessTokenExpiration", -1_000L);
        String raw = jwtService.generateAccessToken("vova@example.com", "ROLE_UNVERIFIED_USER")
                .substring("Bearer ".length());

        assertThat(jwtService.isTokenValid(raw)).isFalse();
    }

    @Test
    @DisplayName("extractRoles возвращает пустой список если ролей в токене нет")
    void extractRoles_noRolesClaim_returnsEmptyList() {
        String raw = jwtService.generateAccessToken("vova@example.com", List.<String>of())
                .substring("Bearer ".length());

        assertThat(jwtService.extractRoles(raw)).isEmpty();
    }

    @Test
    @DisplayName("Токен подписан секретом сервиса (нельзя распарсить другим)")
    void differentSecret_tokenInvalid() {
        String raw = jwtService.generateAccessToken("vova@example.com", "ROLE_UNVERIFIED_USER")
                .substring("Bearer ".length());

        JWTService other = new JWTService();
        ReflectionTestUtils.setField(other, "secret",
                "AnotherTotallyDifferentSecretKeyOfSufficientLengthAAA");
        ReflectionTestUtils.setField(other, "accessTokenExpiration", 60_000L);
        ReflectionTestUtils.setField(other, "refreshTokenExpiration", 60_000L);
        other.init();

        assertThat(other.isTokenValid(raw)).isFalse();
    }
}
