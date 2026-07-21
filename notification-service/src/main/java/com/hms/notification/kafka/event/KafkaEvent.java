package com.hms.notification.kafka.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class KafkaEvent {
    private String eventType;
    private Object payload;
    private LocalDateTime timestamp;
}
