package com.hospital.history.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${hospital.rabbitmq.exchange}")
    private String exchange;

    @Value("${hospital.rabbitmq.queue.appointment-events}")
    private String historyQueue;

    @Value("${hospital.rabbitmq.queue.appointment-dlq}")
    private String historyDlq;

    @Value("${hospital.rabbitmq.routing-key.appointment-all}")
    private String appointmentAllKey;

    @Bean
    public TopicExchange hospitalExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public Queue historyEventsQueue() {
        return QueueBuilder.durable(historyQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", historyDlq)
                .build();
    }

    @Bean
    public Queue historyDeadLetterQueue() {
        return QueueBuilder.durable(historyDlq).build();
    }

    @Bean
    public Binding historyBinding(Queue historyEventsQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(historyEventsQueue)
                .to(hospitalExchange)
                .with(appointmentAllKey);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(jsonMessageConverter());
        return factory;
    }
}
