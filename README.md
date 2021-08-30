# RabbitMQ
This is the spring boot application which can be used as the interface for the rabbitMq. This module is designed such a way that it can publish to multiple queue and it can be subscribe to multiple listners. All these are configurable in the properties file

# Mandatory application properties
1. spring.main.allow-bean-definition-overriding=true
2. spring.rabbitmq.host=localhost
3. spring.rabbitmq.port=5672
4. spring.rabbitmq.username=guest
5. spring.rabbitmq.password=guest

# How to configure multiple publisher dynamically
1. add comma separated value to the property 'rabbitmq.subscriber.configuration.prefix' which will be treated as the prefix for the different configuration.
    1. eg: **rabbitmq.publisher.configuration.prefix=aws,gcp**
    2. in the above example we are creating 2 publishers with the prefix aws and gcp
2. Define Queue, Exchange and RoutingKey in the properties file as follows
    1. Queue : `<prefix>`._rabbitmq.publisher.queue_
    2. Exchange : `<prefix>`._rabbitmq.publisher.exchange_
    3. RoutingKey : `<prefix>`._rabbitmq.publisher.routingKey_
    4. eg:-
        1. To create AWS publisher
            1. **aws.rabbitmq.publisher.exchange**=`<value>`
            2. **aws.rabbitmq.publisher.queue**=`<value>`
            3. **aws.rabbitmq.publisher.routingKey**=`<value>`
        2. To create GCP publisher
            1. **gcp.rabbitmq.publisher.exchange**=`<value>`
            2. **gcp.rabbitmq.publisher.queue**=`<value>`
            3. **gcp.rabbitmq.publisher.routingKey**=`<value>`

# How to publish the message to RabbitMQ
1. Autowired the `RabbitMQSender` in your service
    1. `@Autowired private RabbitMQSender rabbitMQSender;`
2. Send the Message to desired queue using Configuration prefix as follows
    1. `rabbitMQSender.send(<Object>, <ConfigurationPrefix>);`

# How to configure multiple subscriber dynamically
1. add comma separated value to the property 'rabbitmq.subscriber.configuration.prefix' which will be treated as the prefix for the different configuration.
    1. eg: **rabbitmq.subscriber.configuration.prefix=aws,gcp**
    2. in the above example we are creating 2 subscriber with the prefix aws and gcp
2. Define Queue and MessageListener class name in the properties file as follows
    1. Queue : `<prefix>`._rabbitmq.subscriber.queue_
    2. ClassName : `<prefix>`._rabbitmq.subscriber.class.name_ (Class name is the message listener class where you will receive the message from the RabbitMq)
    3. eg:-
        1. To create AWS subscriber
            1. **aws.rabbitmq.subscriber.queue**=`<value>`
            2. **aws.rabbitmq.subscriber.class.name**=`<AWSRabbitMQListener>`
        2. To create GCP subscriber
            1. **gcp.rabbitmq.subscriber.queue**=`<value>`
            2. **gcp.rabbitmq.subscriber.class.name**=`<GCPRabbitMQListener>`

# How to implement the Message Listener
1. Create the service class, Annotate with `@Service(<ListnerName>)` and implement **MessageListener**
2. Implement the method `public void onMessage( Message message )`
   e.g.,
 ````  
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageListener;
import org.springframework.stereotype.Service; 
import lombok.extern.slf4j.Slf4j;

@Service("<ListnerName>")
@Slf4j
public class <ListnerName> implements MessageListener {

    public void onMessage( Message message )
    {
        log.info("Consuming Message- " + new String(message.getBody()));
        //todo Do something on message
    }

}`````