package com.hospital.appointment.config;

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

    @Value("${hospital.rabbitmq.exchange}")
    private String exchange;

    @Value("${hospital.rabbitmq.queue.appointment-events}")
    private String appointmentQueue;

    @Value("${hospital.rabbitmq.queue.appointment-dlq}")
    private String appointmentDlq;

    @Value("${hospital.rabbitmq.routing-key.appointment-created}")
    private String appointmentCreatedKey;

    @Value("${hospital.rabbitmq.routing-key.appointment-updated}")
    private String appointmentUpdatedKey;

    @Value("${hospital.rabbitmq.routing-key.appointment-cancelled}")
    private String appointmentCancelledKey;

    @Bean
    public TopicExchange hospitalExchange() {
        return ExchangeBuilder.topicExchange(exchange).durable(true).build();
    }

    @Bean
    public Queue appointmentEventsQueue() {
        return QueueBuilder.durable(appointmentQueue)
                .withArgument("x-dead-letter-exchange", "")
                .withArgument("x-dead-letter-routing-key", appointmentDlq)
                .withArgument("x-message-ttl", 60000)
                .build();
    }

    @Bean
    public Queue appointmentDeadLetterQueue() {
        return QueueBuilder.durable(appointmentDlq).build();
    }

    @Bean
    public Binding bindingCreated(Queue appointmentEventsQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(appointmentEventsQueue)
                .to(hospitalExchange)
                .with(appointmentCreatedKey);
    }

    @Bean
    public Binding bindingUpdated(Queue appointmentEventsQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(appointmentEventsQueue)
                .to(hospitalExchange)
                .with(appointmentUpdatedKey);
    }

    @Bean
    public Binding bindingCancelled(Queue appointmentEventsQueue, TopicExchange hospitalExchange) {
        return BindingBuilder.bind(appointmentEventsQueue)
                .to(hospitalExchange)
                .with(appointmentCancelledKey);
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
}
