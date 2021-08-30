package com.rmq.service;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service( "GCPRabbitMQListener" )
public class GCPRabbitMQListener implements MessageListener {

    public void onMessage( Message message )
    {
        log.info("Consuming Message GCP- " + new String(message.getBody()));
        //todo Do something on message
    }

}