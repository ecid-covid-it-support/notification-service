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

    @Value("${rabbitmq.exchange}")
    private String exchange;

    @Bean
    public DirectExchange direct() {
        return new DirectExchange(exchange);
    }

    @Value("${rabbitmq.queue.Notification}")
    String queueNotification;

    @Value("${rabbitmq.routingkey.Notification}")
    private String routingkeyNotification;

    @Bean
    Queue queueNotification() {
        return new Queue(queueNotification, false);
    }

    @Bean
    public Binding binding1a(DirectExchange direct, Queue queueNotification) {
        return BindingBuilder.bind(queueNotification).to(direct).with(routingkeyNotification);
    }

    @Value("${rabbitmq.queue.Delete}")
    String queueDelete;

    @Value("${rabbitmq.routingkey.deleteUsers}")
    private String routingkeyDelete;

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