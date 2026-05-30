package ru.rtkmagistral.magistralapi;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

/**
 * Smoke-тест: проверяет, что полный контекст приложения поднимается.
 * Инфраструктура не запускается во внешних сервисах — используется встроенная H2 вместо
 * PostgreSQL (см. {@code classpath:contextloads.properties}), а внешние подключения при старте
 * контекста не выполняются: RabbitMQ-listener'ы отключены свойствами, создание бакета MinIO
 * выключено, а {@link RabbitAdmin} (его дёргает {@code @PostConstruct} в
 * {@link MagistralApiApplication}) подменяется мок-объектом.
 */
@SpringBootTest
@TestPropertySource("classpath:contextloads.properties")
class MagistralApiApplicationTests {

    @MockitoBean
    private RabbitAdmin rabbitAdmin;

    @Test
    void contextLoads() {
    }

}
