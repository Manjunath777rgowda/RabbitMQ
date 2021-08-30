package com.rmq.config;

import static com.rmq.constant.RabbitMQConstant.*;

import java.util.Objects;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.GenericWebApplicationContext;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfiguration {

    @Autowired
    private Environment environment;

    @Autowired
    private GenericWebApplicationContext context;

    @Bean
    ConnectionFactory connectionFactory()
    {
        CachingConnectionFactory cachingConnectionFactory = new CachingConnectionFactory(
                environment.getProperty("spring.rabbitmq.host"));
        cachingConnectionFactory
                .setUsername(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.username")));
        cachingConnectionFactory
                .setUsername(Objects.requireNonNull(environment.getProperty("spring.rabbitmq.password")));
        cachingConnectionFactory.createConnection();
        return cachingConnectionFactory;
    }

    /**
     * This method is used to create the publisher beans dynamically This method read the
     * 'rabbitmq.publisher.configuration.prefix' and create the Queue, Exchange and the Binding using
     * the Routing Key
     */
    @Bean
    public void createPublisherBeans() throws Exception
    {
        String publisherPrefix = environment.getProperty("rabbitmq.publisher.configuration.prefix");
        if( publisherPrefix != null && !publisherPrefix.isEmpty() )
        {
            String[] publisherPrefixList = publisherPrefix.split(",");
            for( String prefix : publisherPrefixList )
            {
                log.info("Configuring '{}' Publisher", prefix);
                //constructing the bean name
                String queueName = environment.getProperty(prefix + PUBLISHER_QUEUE_PROPERTY_NAME);
                String exchangeName = environment.getProperty(prefix + PUBLISHER_EXCHANGE_PROPERTY_NAME);
                String routingName = environment.getProperty(prefix + PUBLISHER_ROUTING_PROPERTY_NAME);

                if( queueName == null || queueName.isEmpty() )
                    throw new Exception("'" + prefix + PUBLISHER_QUEUE_PROPERTY_NAME + "' property Not found");

                if( exchangeName == null || exchangeName.isEmpty() )
                    throw new Exception("'" + prefix + PUBLISHER_EXCHANGE_PROPERTY_NAME + "' property Not found");

                if( routingName == null || routingName.isEmpty() )
                    throw new Exception("'" + prefix + PUBLISHER_ROUTING_PROPERTY_NAME + "' property Not found");

                //Registering the beans to the application context
                String queueBeanName = prefix + QUEUE;
                context.registerBean(queueBeanName, Queue.class, () -> new Queue(queueName, false));
                String exchangeBeanName = prefix + EXCHANGE;
                context.registerBean(exchangeBeanName, DirectExchange.class, () -> new DirectExchange(exchangeName));

                //Fetching the registered beans
                Queue queueBean = (Queue) context.getBean(queueBeanName);
                DirectExchange exchangeBean = (DirectExchange) context.getBean(exchangeBeanName);
                context.registerBean(prefix + BINDING, Binding.class,
                        () -> BindingBuilder.bind(queueBean).to(exchangeBean).with(routingName));

                log.info("Configuring Publisher Queue : '{}'", queueName);
                log.info("Configuring Publisher Exchange : '{}'", exchangeName);
                log.info("Configuring Publisher Routing : '{}'", routingName);
            }
        }
        else
        {
            log.warn("Publisher Not Configured");
        }
    }

    /**
     * This method is used to create the subscriber beans dynamically This method read the
     * 'rabbitmq.subscriber.configuration.prefix' and create the Queue, Exchange and the Binding using
     * the Routing Key
     */
    @Bean
    public void createSubscriberBeans() throws Exception
    {
        String subscriberPrefix = environment.getProperty("rabbitmq.subscriber.configuration.prefix");
        if( subscriberPrefix != null && !subscriberPrefix.isEmpty() )
        {
            String[] subscriberPrefixList = subscriberPrefix.split(",");
            for( String prefix : subscriberPrefixList )
            {
                log.info("Configuring '{}' Subscriber", prefix);

                //constructing the bean name
                String queueName = environment.getProperty(prefix + SUBSCRIBER_QUEUE_PROPERTY_NAME);

                if( queueName == null || queueName.isEmpty() )
                    throw new Exception("'" + prefix + SUBSCRIBER_QUEUE_PROPERTY_NAME + "' property Not found");

                //Registering the beans to the application context
                String queueBeanName = prefix + QUEUE;
                context.registerBean(queueBeanName, Queue.class, () -> new Queue(queueName, false));

                Queue queueBean = (Queue) context.getBean(queueBeanName);

                context.registerBean(prefix + MESSAGE_LISTENER, SimpleMessageListenerContainer.class, () -> {
                    SimpleMessageListenerContainer simpleMessageListenerContainer = new SimpleMessageListenerContainer();
                    simpleMessageListenerContainer.setConnectionFactory(connectionFactory());
                    simpleMessageListenerContainer.setQueues(queueBean);
                    simpleMessageListenerContainer.setMessageListener((MessageListener) context.getBean(Objects
                            .requireNonNull(environment.getProperty(prefix + SUBSCRIBER_MESSAGE_LISTENER_CLASS_NAME))));
                    return simpleMessageListenerContainer;
                });

                log.info("Configured Subscriber Queue : '{}'", queueName);
            }
        }
        else
        {
            log.warn("Subscriber Not Configured");
        }
    }

    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public AmqpTemplate rabbitTemplate()
    {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory());
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
