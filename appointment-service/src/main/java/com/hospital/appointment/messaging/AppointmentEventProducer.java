package com.hospital.appointment.messaging;

import com.hospital.appointment.dto.event.AppointmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventProducer {

    private final RabbitTemplate rabbitTemplate;

    @Value("${hospital.rabbitmq.exchange}")
    private String exchange;

    @Value("${hospital.rabbitmq.routing-key.appointment-created}")
    private String createdRoutingKey;

    @Value("${hospital.rabbitmq.routing-key.appointment-updated}")
    private String updatedRoutingKey;

    @Value("${hospital.rabbitmq.routing-key.appointment-cancelled}")
    private String cancelledRoutingKey;

    public void publishCreated(AppointmentEvent event) {
        publish(createdRoutingKey, event);
    }

    public void publishUpdated(AppointmentEvent event) {
        publish(updatedRoutingKey, event);
    }

    public void publishCancelled(AppointmentEvent event) {
        publish(cancelledRoutingKey, event);
    }

    private void publish(String routingKey, AppointmentEvent event) {
        try {
            log.info("Publishing event [{}] for appointment [{}]", event.getEventType(), event.getAppointmentId());
            rabbitTemplate.convertAndSend(exchange, routingKey, event);
            log.info("Event published successfully");
        } catch (Exception e) {
            log.error("Failed to publish event [{}]: {}", event.getEventType(), e.getMessage(), e);
        }
    }
}
