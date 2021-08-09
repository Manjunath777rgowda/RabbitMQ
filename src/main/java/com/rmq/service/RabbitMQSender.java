package com.rmq.service;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import static com.rmq.constant.RabbitMQConstant.*;

@Service
public class RabbitMQSender {

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private Environment environment;

    public <T> void send( T object, String prefix )
    {
        amqpTemplate.convertAndSend(environment.getProperty(prefix + PUBLISHER_EXCHANGE_PROPERTY_NAME),
                environment.getProperty(prefix + PUBLISHER_ROUTING_PROPERTY_NAME), object);
    }
}