package ru.rtkmagistral.magistralapi.support;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

import static org.mockito.Mockito.mock;

/**
 * Тестовый конфиг для срезов контроллеров. Подменяет инфраструктурные бины (AMQP),
 * которые тянет MagistralApiApplication, чтобы контекст WebMvcTest успешно поднимался.
 * Также определяет permit-all SecurityFilterChain — основная SecurityConfig не подгружается
 * (она требует JWT-фильтр, UserDetailsService и т.д.), поэтому без своей цепочки
 * Spring Boot включил бы базовую аутентификацию и все запросы получали бы 401.
 * {@link EnableMethodSecurity} нужно для срабатывания {@code @PreAuthorize}/@ForVerifiedUsers и т.п.
 */
@TestConfiguration
@EnableMethodSecurity
public class WebTestSupport {

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return mock(RabbitAdmin.class);
    }

    @Bean(name = "confirmQueue")
    public Queue confirmQueue() {
        return new Queue("confirm-queue-test");
    }

    @Bean(name = "documentQueue")
    public Queue documentQueue() {
        return new Queue("document-queue-test");
    }

    @Bean(name = "minioQueue")
    public Queue minioQueue() {
        return new Queue("minio-queue-test");
    }

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }
}
