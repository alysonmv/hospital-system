package com.hospital.history.messaging;

import com.hospital.history.messaging.dto.AppointmentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AppointmentEventConsumer {

    private final AppointmentSyncService syncService;

    @RabbitListener(queues = "${hospital.rabbitmq.queue.appointment-events}")
    public void consume(AppointmentEvent event) {
        log.info("History-service received event [{}] for appointment [{}]",
                event.getEventType(), event.getAppointmentId());
        try {
            syncService.syncAppointment(event);
        } catch (Exception e) {
            log.error("Failed to sync appointment [{}]: {}", event.getAppointmentId(), e.getMessage(), e);
            throw e; // triggers retry
        }
    }
}
