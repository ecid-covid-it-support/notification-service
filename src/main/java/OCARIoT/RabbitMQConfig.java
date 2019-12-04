package OCARIoT;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

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
        return new Queue(queueNotification, false);
    }

    @Bean
    public Binding binding1a(DirectExchange directNotification, Queue queueNotification) {
        return BindingBuilder.bind(queueNotification).to(directNotification).with(routingkeyNotification);
    }

    @Value("${rabbitmq.exchange.delete.users}")
    private String exchange;

    @Bean
    public DirectExchange direct() {
        return new DirectExchange(exchange);
    }

    @Value("${rabbitmq.routingkey.delete.users}")
    private String routingkeyDelete;

    @Value("${rabbitmq.queue.delete.users}")
    String queueDelete;

    @Bean
    Queue queueUsers() {
        return new Queue(queueDelete, false);
    }

    @Bean
    public Binding binding2a(DirectExchange direct, Queue queueUsers) {
        return BindingBuilder.bind(queueUsers).to(direct).with(routingkeyDelete);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public AmqpTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}