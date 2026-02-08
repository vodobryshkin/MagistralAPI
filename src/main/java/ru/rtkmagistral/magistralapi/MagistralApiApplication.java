package ru.rtkmagistral.magistralapi;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MagistralApiApplication {
    private final RabbitAdmin rabbitAdmin;

    private final Queue confirmQueue;
    private final Queue documentQueue;
    private final Queue minioQueue;

    public MagistralApiApplication(RabbitAdmin rabbitAdmin,
                                   @Qualifier("confirmQueue") Queue confirmQueue,
                                   @Qualifier("documentQueue") Queue documentQueue,
                                   @Qualifier("minioQueue") Queue minioQueue
    ) {
        this.rabbitAdmin = rabbitAdmin;
        this.confirmQueue = confirmQueue;
        this.documentQueue = documentQueue;
        this.minioQueue = minioQueue;
    }

    @PostConstruct
    public void init() {
        rabbitAdmin.declareQueue(confirmQueue);
        rabbitAdmin.declareQueue(documentQueue);
        rabbitAdmin.declareQueue(minioQueue);
    }

    public static void main(String[] args) {
        SpringApplication.run(MagistralApiApplication.class, args);
    }

}
