package com.hms.notification.repository;

import com.hms.notification.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    Optional<Notification> findByNotificationId(String notificationId);
    List<Notification> findByRecipientIdAndRecipientType(Long recipientId, String recipientType);
}
