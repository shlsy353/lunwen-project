package com.project.mq;

import com.project.config.*;
import lombok.extern.slf4j.*;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.stereotype.*;

@Slf4j
@Component
public class RegistrationMessageListener {



    @RabbitListener(queues = RabbitMQConfig.REGISTRATION_QUEUE)
    public void handleRegistrationMessage(String message) {
        log.info("Received registration message: " + message);
    }
}
