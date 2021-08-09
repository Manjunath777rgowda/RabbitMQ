package com.middleware.nuvelink.rmq.constant;

public class RabbitMQConstant {

    public static final String PUBLISHER_EXCHANGE_PROPERTY_NAME = ".rabbitmq.publisher.exchange";
    public static final String PUBLISHER_QUEUE_PROPERTY_NAME = ".rabbitmq.publisher.queue";
    public static final String PUBLISHER_ROUTING_PROPERTY_NAME = ".rabbitmq.publisher.routingKey";

    public static final String SUBSCRIBER_QUEUE_PROPERTY_NAME = ".rabbitmq.subscriber.queue";

    public static final String EXCHANGE = "Exchange";
    public static final String QUEUE = "Queue";
    public static final String BINDING = "Binding";
    public static final String MESSAGE_LISTENER = "MessageListener";
}
