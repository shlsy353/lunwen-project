package com.project.config;


import java.util.*;
import lombok.*;
import org.springframework.context.annotation.*;
import org.springframework.amqp.core.Queue;

@Configuration

public class RabbitMQConfig {


    public static final String REGISTRATION_QUEUE = "registration.queue";

    @Bean
    public Queue registrationQueue() {
        return new Queue(REGISTRATION_QUEUE,true);
    }
}
