package com.hms.notification.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NotificationResponse {
    private String notificationId;
    private Long recipientId;
    private String recipientType;
    private String notificationType;
    private String channel;
    private String subject;
    private String message;
    private String deliveryStatus;
    private LocalDateTime sentAt;
}
