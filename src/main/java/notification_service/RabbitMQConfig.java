package notification_service;


import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {


    @Value("${rabbitmq.exchange.delete.users}")
    private String exchangeDeleteUsers;

    @Bean
    public DirectExchange directDeleteUsers() {

        return new DirectExchange(exchangeDeleteUsers);
    }

    @Value("${rabbitmq.routingkey.delete.users}")
    private String routingkeyDeleteUsers;


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
        return BindingBuilder.bind(queueNotification).to(directNotification).with(routingkeyNotification);
    }

    @Bean
    public Binding binding1b(DirectExchange directDeleteUsers, Queue queueNotification) {
        return BindingBuilder.bind(queueNotification).to(directDeleteUsers).with(routingkeyDeleteUsers);
    }

}