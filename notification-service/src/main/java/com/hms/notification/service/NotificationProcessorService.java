package com.hms.notification.service;

import com.hms.notification.entity.Notification;
import com.hms.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProcessorService {

    private final NotificationRepository notificationRepository;
    private final NotificationSenderService senderService;

    public void processAppointmentCreated(Map<String, Object> payload) {
        Long patientId = extractLong(payload, "patientId");
        String doctorName = "the designated Doctor"; // In real scenario, fetch from DoctorService if needed
        
        String message = String.format("Dear Patient,\n\nYour appointment with %s has been confirmed.\nThank you.", doctorName);
        saveAndSendNotification(patientId, "PATIENT", "APPOINTMENT_CREATED", "Appointment Confirmation", message, "EMAIL");
    }

    public void processAppointmentReminder(Map<String, Object> payload) {
        Long patientId = extractLong(payload, "patientId");
        String message = "Reminder: You have an upcoming appointment tomorrow. Please arrive 15 minutes early.";
        saveAndSendNotification(patientId, "PATIENT", "APPOINTMENT_REMINDER", "Appointment Reminder", message, "SMS");
    }

    public void processPrescriptionCreated(Map<String, Object> payload) {
        Long patientId = extractLong(payload, "patientId");
        String message = "Your prescription is now available. Please log in to the Hospital Portal to view and download your prescription.";
        saveAndSendNotification(patientId, "PATIENT", "PRESCRIPTION_READY", "Prescription Available", message, "EMAIL");
    }

    public void processPaymentSuccess(Map<String, Object> payload) {
        Long patientId = extractLong(payload, "patientId");
        Object paidAmount = payload.get("paidAmount");
        String message = String.format("Payment Successful.\nAmount Paid: ₹%s\nThank you.", paidAmount);
        saveAndSendNotification(patientId, "PATIENT", "PAYMENT_SUCCESS", "Payment Confirmation", message, "EMAIL");
    }

    public void processPaymentFailed(Map<String, Object> payload) {
        Long patientId = extractLong(payload, "patientId");
        String message = "Your recent payment attempt has failed. Please try again or contact support.";
        saveAndSendNotification(patientId, "PATIENT", "PAYMENT_FAILED", "Payment Failed", message, "SMS");
    }

    private void saveAndSendNotification(Long recipientId, String recipientType, String type, String subject, String message, String channel) {
        Notification notification = Notification.builder()
                .notificationId(generateNotificationId())
                .recipientId(recipientId)
                .recipientType(recipientType)
                .notificationType(type)
                .channel(channel)
                .subject(subject)
                .message(message)
                .deliveryStatus("PENDING")
                .build();

        Notification savedNotification = notificationRepository.save(notification);

        try {
            senderService.sendNotification(savedNotification);
            savedNotification.setDeliveryStatus("SENT");
            savedNotification.setSentAt(LocalDateTime.now());
        } catch (Exception e) {
            log.error("Failed to send notification {}", savedNotification.getNotificationId(), e);
            savedNotification.setDeliveryStatus("FAILED");
        }
        
        notificationRepository.save(savedNotification);
    }

    private String generateNotificationId() {
        long count = notificationRepository.count() + 1;
        return String.format("NOT%06d", count);
    }

    private Long extractLong(Map<String, Object> payload, String key) {
        Object value = payload.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        } else if (value instanceof String) {
            return Long.parseLong((String) value);
        }
        return 0L; // Fallback
    }
}
