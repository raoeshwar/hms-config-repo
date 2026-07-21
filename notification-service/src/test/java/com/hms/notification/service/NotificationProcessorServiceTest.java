package com.hms.notification.service;

import com.hms.notification.entity.Notification;
import com.hms.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationProcessorServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationSenderService senderService;

    @InjectMocks
    private NotificationProcessorService processorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void processAppointmentCreated_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("patientId", 101L);

        Notification mockNotification = new Notification();
        mockNotification.setNotificationId("NOT000001");

        when(notificationRepository.count()).thenReturn(0L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);
        doNothing().when(senderService).sendNotification(any(Notification.class));

        processorService.processAppointmentCreated(payload);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(senderService, times(1)).sendNotification(any(Notification.class));
    }

    @Test
    void processPaymentSuccess_Success() {
        Map<String, Object> payload = new HashMap<>();
        payload.put("patientId", 101L);
        payload.put("paidAmount", "1800.00");

        Notification mockNotification = new Notification();
        mockNotification.setNotificationId("NOT000002");

        when(notificationRepository.count()).thenReturn(1L);
        when(notificationRepository.save(any(Notification.class))).thenReturn(mockNotification);
        doNothing().when(senderService).sendNotification(any(Notification.class));

        processorService.processPaymentSuccess(payload);

        verify(notificationRepository, times(2)).save(any(Notification.class));
        verify(senderService, times(1)).sendNotification(any(Notification.class));
    }
}
