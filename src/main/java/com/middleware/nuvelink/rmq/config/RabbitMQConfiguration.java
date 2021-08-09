package com.middleware.nuvelink.rmq.config;

import static com.middleware.nuvelink.rmq.constant.RabbitMQConstant.*;

import java.util.List;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.middleware.nuvelink.rmq.service.AWSRabbitMQListener;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class RabbitMQConfiguration {

    @Value( "#{'${rabbitmq.publisher.configuration.prefix}'.split(',')}" )
    private List<String> publisherPrefixList;

    @Value( "#{'${rabbitmq.subscriber.configuration.prefix}'.split(',')}" )
    private List<String> subscriberPrefixList;

    @Autowired
    private Environment environment;

    @Autowired
    private GenericWebApplicationContext context;

    @Autowired
    private ConnectionFactory connectionFactory;

    /**
     * This method is used to create the publisher beans dynamically This method read the
     * 'rabbitmq.publisher.configuration.prefix' and create the Queue, Exchange and the Binding using
     * the Routing Key
     * 
     */
    @Bean
    public void createPublisherBeans() throws Exception
    {
        for( String prefix : publisherPrefixList )
        {
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
        }
    }

    /**
     * This method is used to create the subscriber beans dynamically This method read the
     * 'rabbitmq.subscriber.configuration.prefix' and create the Queue, Exchange and the Binding using
     * the Routing Key
     *
     */
    @Bean
    public void createSubscriberBeans() throws Exception
    {
        for( String prefix : subscriberPrefixList )
        {
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
                simpleMessageListenerContainer.setConnectionFactory(connectionFactory);
                simpleMessageListenerContainer.setQueues(queueBean);
                simpleMessageListenerContainer.setMessageListener(new AWSRabbitMQListener());
                return simpleMessageListenerContainer;
            });
        }
    }

    @Bean
    public MessageConverter jsonMessageConverter()
    {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    @Primary
    public AmqpTemplate rabbitTemplate( ConnectionFactory connectionFactory )
    {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
