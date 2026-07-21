package com.hms.billing.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class BillingEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishBillingEvent(String topic, String billNumber, String eventType, Object payload) {
        log.info("Publishing event to topic {}: {}", topic, eventType);
        BillingEvent event = new BillingEvent(billNumber, eventType, payload, LocalDateTime.now());
        kafkaTemplate.send(topic, billNumber, event);
    }

    @Data
    @AllArgsConstructor
    public static class BillingEvent {
        private String billNumber;
        private String eventType;
        private Object payload;
        private LocalDateTime timestamp;
    }

    @Data
    @AllArgsConstructor
    public static class BillGeneratedEvent {
        private Long patientId;
        private BigDecimal totalAmount;
        private BigDecimal balanceAmount;
    }

    @Data
    @AllArgsConstructor
    public static class PaymentEvent {
        private Long patientId;
        private BigDecimal paidAmount;
        private String paymentStatus;
    }
}
