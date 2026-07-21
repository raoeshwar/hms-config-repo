package com.hms.notification.service;

import com.hms.notification.entity.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationSenderService {

    public void sendNotification(Notification notification) {
        log.info("Sending notification via channel: {}", notification.getChannel());
        
        switch (notification.getChannel().toUpperCase()) {
            case "EMAIL":
                sendEmail(notification);
                break;
            case "SMS":
                sendSms(notification);
                break;
            case "PUSH":
                sendPushNotification(notification);
                break;
            default:
                throw new IllegalArgumentException("Unsupported channel: " + notification.getChannel());
        }
    }

    private void sendEmail(Notification notification) {
        // Simulate sending email
        log.info("Email sent to patient ID {} with subject: '{}'", notification.getRecipientId(), notification.getSubject());
        log.debug("Email Body: \n{}", notification.getMessage());
    }

    private void sendSms(Notification notification) {
        // Simulate sending SMS via Twilio
        log.info("SMS sent to patient ID {}: '{}'", notification.getRecipientId(), notification.getMessage());
    }

    private void sendPushNotification(Notification notification) {
        // Simulate Firebase push
        log.info("Push notification sent to patient ID {}: '{}'", notification.getRecipientId(), notification.getSubject());
    }
}
