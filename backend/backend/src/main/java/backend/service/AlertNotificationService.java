package backend.service;

import backend.dto.AlertNotification;
import backend.entity.Alert;
import backend.entity.Notification;
import backend.entity.User;
import backend.repository.NotificationRepository;
import backend.repository.UserRepository;
import backend.reports.util.EmailUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AlertNotificationService {

    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private SettingsService settingsService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EmailUtil emailUtil;

    public AlertNotificationService(
            SimpMessagingTemplate messagingTemplate,
            NotificationService notificationService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.notificationService = notificationService;
    }

    public void sendNotification(Alert alert) {
        // 1. Throttle check to prevent alert storms
        try {
            int throttleMinutes = Integer.parseInt(settingsService.getValue("NOTIFICATION_THROTTLE_MINUTES", "5"));
            Optional<Notification> lastOpt = notificationRepository.findFirstByTitleOrderByCreatedAtDesc(alert.getTitle());
            if (lastOpt.isPresent()) {
                long diff = Duration.between(lastOpt.get().getCreatedAt(), LocalDateTime.now()).toMinutes();
                if (diff < throttleMinutes) {
                    System.out.println("Notification for alert '" + alert.getTitle() + "' throttled. Time since last: " + diff + " mins. Threshold: " + throttleMinutes + " mins.");
                    return;
                }
            }
        } catch (Exception e) {
            System.err.println("Throttling logic encountered error: " + e.getMessage());
        }

        // 2. Save notification to database
        Notification notification = notificationService.saveNotification(
                alert.getTitle(),
                alert.getSeverity(),
                "New alert generated: " + alert.getTitle()
        );

        // 3. Dispatch to multi-channels: In-App (WebSocket), Email, Webhook
        dispatchInApp(alert, notification);
        dispatchEmails(alert);
        dispatchWebhook(alert);
    }

    private void dispatchInApp(Alert alert, Notification notification) {
        try {
            AlertNotification alertNotification = new AlertNotification(
                    notification.getId(),
                    notification.getTitle(),
                    notification.getSeverity(),
                    alert.getStatus(),
                    notification.getMessage(),
                    notification.getCreatedAt()
            );

            messagingTemplate.convertAndSend(
                    "/topic/alerts",
                    alertNotification
            );
        } catch (Exception e) {
            System.err.println("Failed to broadcast WebSocket notification: " + e.getMessage());
        }
    }

    private void dispatchEmails(Alert alert) {
        try {
            boolean emailEnabled = Boolean.parseBoolean(settingsService.getValue("EMAIL_ENABLED", "true"));
            if (!emailEnabled) {
                return;
            }

            List<User> users = userRepository.findAll();
            for (User user : users) {
                if (user.getRole() != null && 
                    ("ADMIN".equalsIgnoreCase(user.getRole().getName()) || "ANALYST".equalsIgnoreCase(user.getRole().getName()))) {
                    
                    Boolean userEmailEnabled = user.getEmailNotificationsEnabled();
                    if (userEmailEnabled != null && !userEmailEnabled) {
                        continue;
                    }

                    String subject = "🚨 SentinelCore Security Alert: [" + alert.getSeverity() + "] " + alert.getTitle();
                    String body = "Hello " + user.getName() + ",\n\n" +
                            "A new security alert has been triggered in SentinelCore:\n\n" +
                            "Alert Title: " + alert.getTitle() + "\n" +
                            "Severity: " + alert.getSeverity() + "\n" +
                            "Source: " + alert.getSource() + "\n" +
                            "Status: " + alert.getStatus() + "\n" +
                            "Occurred At: " + LocalDateTime.now() + "\n\n" +
                            "Description:\n" + alert.getDescription() + "\n\n" +
                            "Please log in to the SentinelCore SOC Dashboard to investigate.\n\n" +
                            "Best regards,\n" +
                            "SentinelCore Notification Service";

                    try {
                        emailUtil.sendReportEmail(user.getEmail(), subject, body, null);
                    } catch (Exception emailEx) {
                        System.err.println("Failed to send email to " + user.getEmail() + ": " + emailEx.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to dispatch alert emails: " + e.getMessage());
        }
    }

    private void dispatchWebhook(Alert alert) {
        try {
            boolean webhookEnabled = Boolean.parseBoolean(settingsService.getValue("WEBHOOK_ENABLED", "false"));
            if (!webhookEnabled) {
                return;
            }
            String webhookUrl = settingsService.getValue("WEBHOOK_URL", "");
            if (webhookUrl == null || webhookUrl.trim().isEmpty()) {
                return;
            }

            Map<String, Object> payload = new HashMap<>();
            payload.put("alertId", alert.getId());
            payload.put("title", alert.getTitle());
            payload.put("severity", alert.getSeverity());
            payload.put("source", alert.getSource());
            payload.put("status", alert.getStatus());
            payload.put("description", alert.getDescription());
            payload.put("timestamp", LocalDateTime.now().toString());

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            restTemplate.postForEntity(webhookUrl, payload, String.class);
            System.out.println("Alert webhook successfully dispatched to: " + webhookUrl);
        } catch (Exception e) {
            System.err.println("Failed to dispatch alert webhook: " + e.getMessage());
        }
    }
}