package com.hms.notification.kafka.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hms.notification.service.NotificationProcessorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    private final NotificationProcessorService notificationProcessor;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "appointment-created", groupId = "notification-group")
    public void consumeAppointmentCreated(String message) {
        log.info("Received appointment-created event: {}", message);
        try {
            Map<String, Object> payload = extractPayload(message);
            notificationProcessor.processAppointmentCreated(payload);
        } catch (Exception e) {
            log.error("Failed to process appointment-created event", e);
        }
    }

    @KafkaListener(topics = "appointment-reminder", groupId = "notification-group")
    public void consumeAppointmentReminder(String message) {
        log.info("Received appointment-reminder event: {}", message);
        try {
            Map<String, Object> payload = extractPayload(message);
            notificationProcessor.processAppointmentReminder(payload);
        } catch (Exception e) {
            log.error("Failed to process appointment-reminder event", e);
        }
    }

    @KafkaListener(topics = "prescription-created", groupId = "notification-group")
    public void consumePrescriptionCreated(String message) {
        log.info("Received prescription-created event: {}", message);
        try {
            Map<String, Object> payload = extractPayload(message);
            notificationProcessor.processPrescriptionCreated(payload);
        } catch (Exception e) {
            log.error("Failed to process prescription-created event", e);
        }
    }

    @KafkaListener(topics = "payment-success", groupId = "notification-group")
    public void consumePaymentSuccess(String message) {
        log.info("Received payment-success event: {}", message);
        try {
            Map<String, Object> payload = extractPayload(message);
            notificationProcessor.processPaymentSuccess(payload);
        } catch (Exception e) {
            log.error("Failed to process payment-success event", e);
        }
    }

    @KafkaListener(topics = "payment-failed", groupId = "notification-group")
    public void consumePaymentFailed(String message) {
        log.info("Received payment-failed event: {}", message);
        try {
            Map<String, Object> payload = extractPayload(message);
            notificationProcessor.processPaymentFailed(payload);
        } catch (Exception e) {
            log.error("Failed to process payment-failed event", e);
        }
    }

    private Map<String, Object> extractPayload(String message) throws JsonProcessingException {
        Map<String, Object> eventMap = objectMapper.readValue(message, new TypeReference<Map<String, Object>>() {});
        if (eventMap.containsKey("payload")) {
            return objectMapper.convertValue(eventMap.get("payload"), new TypeReference<Map<String, Object>>() {});
        }
        return eventMap;
    }
}
