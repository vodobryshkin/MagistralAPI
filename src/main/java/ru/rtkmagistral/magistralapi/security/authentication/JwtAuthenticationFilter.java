package ru.rtkmagistral.magistralapi.security.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.rtkmagistral.magistralapi.service.spec.IJWTService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final IJWTService jwtService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(BEARER_PREFIX.length());

        // extractUsername() выполняет parseSignedClaims(), который уже проверяет
        // и подпись, и срок действия токена (бросает ExpiredJwtException на просроченном
        // и JwtException на некорректном). Поэтому отдельный вызов isTokenValid() здесь не нужен —
        // это был второй полный разбор токена с повторной проверкой HMAC-подписи на каждом запросе.
        String username;
        try {
            username = jwtService.extractUsername(token);
        } catch (ExpiredJwtException e) {
            writeUnauthorized(response, request, "ACCESS_TOKEN_HAS_EXPIRED");
            return;
        } catch (JwtException | IllegalArgumentException e) {
            writeUnauthorized(response, request, "ACCESS_TOKEN_INVALID");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // Роли уже лежат в подписанных claims токена (кладутся при выпуске в JWTService),
            // а подпись токена уже проверена выше при extractUsername(). Поэтому Authentication
            // строится прямо из токена, без обращения к UserDetailsService — это убирает 2 SQL-запроса
            // (поиск пользователя по email + его ролей) на КАЖДЫЙ аутентифицированный запрос.
            // Принципал — это email (username): authentication.getName() возвращает его, как и раньше.
            // Компромисс: изменение ролей/удаление пользователя вступит в силу только после
            // перевыпуска access-токена (в пределах его времени жизни).
            List<SimpleGrantedAuthority> authorities = jwtService.extractRoles(token).stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken authentication =
                    UsernamePasswordAuthenticationToken.authenticated(
                            username,
                            null,
                            authorities
                    );

            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response,
                                   HttpServletRequest request,
                                   String message) throws IOException {

        if (response.isCommitted()) return;

        response.resetBuffer();
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        objectMapper.writeValue(response.getOutputStream(), Map.of(
                "message", message
        ));

        response.flushBuffer();
    }
}
