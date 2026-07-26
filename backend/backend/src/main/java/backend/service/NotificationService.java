package backend.service;

import backend.entity.Incident;
import backend.entity.Notification;
import backend.entity.PlaybookExecution;
import backend.entity.PlaybookExecutionLog;
import backend.repository.IncidentRepository;
import backend.repository.NotificationRepository;
import backend.repository.PlaybookExecutionLogRepository;
import backend.repository.PlaybookExecutionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@SuppressWarnings("null")
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final PlaybookExecutionRepository playbookExecutionRepository;
    private final IncidentRepository incidentRepository;
    private final PlaybookExecutionLogRepository playbookExecutionLogRepository;

    public NotificationService(NotificationRepository notificationRepository,
                               PlaybookExecutionRepository playbookExecutionRepository,
                               IncidentRepository incidentRepository,
                               PlaybookExecutionLogRepository playbookExecutionLogRepository) {
        this.notificationRepository = notificationRepository;
        this.playbookExecutionRepository = playbookExecutionRepository;
        this.incidentRepository = incidentRepository;
        this.playbookExecutionLogRepository = playbookExecutionLogRepository;
    }

    public Notification saveNotification(String title,
                                         String severity,
                                         String message) {

        Notification notification = new Notification(
                title,
                severity,
                message,
                false,
                LocalDateTime.now()
        );

        return notificationRepository.save(notification);
    }

    public Notification saveNotification(String title,
                                         String severity,
                                         String message,
                                         Long playbookExecutionId) {

        Notification notification = new Notification(
                title,
                severity,
                message,
                false,
                LocalDateTime.now()
        );
        notification.setPlaybookExecutionId(playbookExecutionId);

        return notificationRepository.save(notification);
    }

    public List<Notification> getAllNotifications() {
        return notificationRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Notification> getUnreadNotifications() {
        return notificationRepository.findByReadStatusFalseOrderByCreatedAtDesc();
    }

    public Notification markAsRead(Long id) {

        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setReadStatus(true);
        Notification saved = notificationRepository.save(notification);

        if (saved.getPlaybookExecutionId() != null) {
            completeAssetOfflinePlaybook(saved.getPlaybookExecutionId());
        }

        return saved;
    }

    public void markAllAsRead() {

        List<Notification> notifications =
                notificationRepository.findByReadStatusFalseOrderByCreatedAtDesc();

        notifications.forEach(n -> {
            n.setReadStatus(true);
            if (n.getPlaybookExecutionId() != null) {
                completeAssetOfflinePlaybook(n.getPlaybookExecutionId());
            }
        });

        notificationRepository.saveAll(notifications);
    }

    public void clearAll() {
        notificationRepository.deleteAll();
    }

    private void completeAssetOfflinePlaybook(Long executionId) {
        PlaybookExecution execution = playbookExecutionRepository.findById(executionId).orElse(null);
        if (execution == null || !"RUNNING".equals(execution.getStatus())) {
            return;
        }

        execution.setStatus("SUCCESS");
        execution.setProgress(100);
        execution.setCurrentStep("Execution Completed");
        execution.setEndedAt(LocalDateTime.now());
        playbookExecutionRepository.save(execution);

        // Write execution logs
        PlaybookExecutionLog log1 = PlaybookExecutionLog.builder()
                .playbookExecution(execution)
                .stepName("Send Asset Offline Notification")
                .status("SUCCESS")
                .logLevel("INFO")
                .message("Notification seen by Administrator. Playbook execution acknowledged.")
                .build();
        playbookExecutionLogRepository.save(log1);

        PlaybookExecutionLog log2 = PlaybookExecutionLog.builder()
                .playbookExecution(execution)
                .stepName("System")
                .status("SUCCESS")
                .logLevel("INFO")
                .message("Playbook finished execution with status SUCCESS.")
                .build();
        playbookExecutionLogRepository.save(log2);

        // Resolve associated incident
        Incident targetIncident = execution.getIncident();
        if (targetIncident == null && execution.getIncidentId() != null) {
            targetIncident = incidentRepository.findById(execution.getIncidentId()).orElse(null);
        }
        if (targetIncident != null) {
            targetIncident.setStatus("Resolved");
            targetIncident.setDescription(targetIncident.getDescription() +
                    "\n\n[Playbook Automation] This incident has been automatically resolved because the Administrator has seen/acknowledged the offline alert notification.");
            incidentRepository.save(targetIncident);
        }
    }
}