package com.hms.notification.controller;

import com.hms.notification.dto.ApiResponse;
import com.hms.notification.dto.NotificationResponse;
import com.hms.notification.entity.Notification;
import com.hms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    @GetMapping("/{notificationId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SYSTEM')")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(@PathVariable String notificationId) {
        return notificationRepository.findByNotificationId(notificationId)
                .map(n -> ResponseEntity.ok(ApiResponse.success("Notification retrieved successfully.", mapToResponse(n))))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PATIENT')")
    public ResponseEntity<ApiResponse<List<NotificationResponse>>> getPatientNotifications(@PathVariable Long patientId) {
        List<NotificationResponse> notifications = notificationRepository.findByRecipientIdAndRecipientType(patientId, "PATIENT").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success("Patient notifications retrieved successfully.", notifications));
    }

    private NotificationResponse mapToResponse(Notification notification) {
        return NotificationResponse.builder()
                .notificationId(notification.getNotificationId())
                .recipientId(notification.getRecipientId())
                .recipientType(notification.getRecipientType())
                .notificationType(notification.getNotificationType())
                .channel(notification.getChannel())
                .subject(notification.getSubject())
                .message(notification.getMessage())
                .deliveryStatus(notification.getDeliveryStatus())
                .sentAt(notification.getSentAt())
                .build();
    }
}
