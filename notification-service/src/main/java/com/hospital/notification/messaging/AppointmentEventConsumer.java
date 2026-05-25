package com.hospital.notification.messaging;

import com.hospital.notification.dto.AppointmentEvent;
import com.hospital.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventConsumer {

    private final NotificationService notificationService;

    @RabbitListener(bindings = @QueueBinding(
        value = @Queue(value = "appointment.events.queue", durable = "true"),
        exchange = @Exchange(value = "hospital.exchange", type = "topic"),
        key = "appointment.*"
    ))
    public void consumeAppointmentEvent(AppointmentEvent event) {
        log.info("Received event [{}] for appointment [{}]", event.getEventType(), event.getAppointmentId());
        switch (event.getEventType()) {
            case "CREATED"   -> notificationService.processAppointmentCreated(event);
            case "UPDATED"   -> notificationService.processAppointmentUpdated(event);
            case "CANCELLED" -> notificationService.processAppointmentCancelled(event);
            default -> log.warn("Unknown event type: {}", event.getEventType());
        }
    }
}
