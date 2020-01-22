package OCARIoT;

import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Level;
import java.util.logging.Logger;

@Configuration
public class RabbitMQConfig {

    private static final Logger LOGGER = Logger.getLogger( RabbitMQ.class.getName() );

    @Value("${rabbitmq.exchange.send.notification}")
    private String exchangeNotification;

    @Bean
    public DirectExchange directNotification() {

        return new DirectExchange(exchangeNotification);
    }

    @Value("${rabbitmq.queue.send.notification}")
    String queueNotification;

    @Value("${rabbitmq.routingkey.send.notification}")
    private String routingkeyNotification;

    @Bean
    Queue queueNotification() {

        return new Queue(queueNotification, true);
    }

    @Bean
    public Binding binding1a(DirectExchange directNotification, Queue queueNotification) {

        Binding binder = null;
        try{

            binder = BindingBuilder.bind(queueNotification).to(directNotification).with(routingkeyNotification);
            LOGGER.log(Level.WARNING, "Connection established.");

        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Trying to establish connection.");
        }
        return binder;
    }

}