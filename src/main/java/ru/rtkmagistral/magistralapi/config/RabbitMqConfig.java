package ru.rtkmagistral.magistralapi.config;

import lombok.Setter;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.JacksonJsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация для корректной работы очереди сообщений на основе Rabbit MQ
 */
@Configuration
@Setter
public class RabbitMqConfig {
    @Value("${mail.confirm.queue.name}")
    private String confirmQueueName;

    @Value("${mail.document.queue.name}")
    private String documentQueueName;

    @Value("${minio.queue}")
    private String minioQueueName;

    @Value("${spring.rabbitmq.username}")
    private String username;

    @Value("${spring.rabbitmq.password}")
    private String password;

    @Value("${spring.rabbitmq.host}")
    private String host;

    @Bean
    public Queue confirmQueue() {
        return new Queue(confirmQueueName);
    }

    @Bean
    public Queue documentQueue() {
        return new Queue(documentQueueName);
    }

    @Bean
    public Queue minioQueue() {
        return new Queue(minioQueueName);
    }

    @Bean
    public CachingConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory(host);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        return connectionFactory;
    }

    @Bean
    public RabbitAdmin rabbitAdmin() {
        return new RabbitAdmin(connectionFactory());
    }

    @Bean
    public JacksonJsonMessageConverter jackson2JsonMessageConverter() {
        return new JacksonJsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory cf, JacksonJsonMessageConverter conv) {
        RabbitTemplate rt = new RabbitTemplate(cf);
        rt.setMessageConverter(conv);
        return rt;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory cf,
            JacksonJsonMessageConverter conv
    ) {
        SimpleRabbitListenerContainerFactory f = new SimpleRabbitListenerContainerFactory();
        f.setConnectionFactory(cf);
        f.setMessageConverter(conv);
        f.setDefaultRequeueRejected(false);
        return f;
    }
}
