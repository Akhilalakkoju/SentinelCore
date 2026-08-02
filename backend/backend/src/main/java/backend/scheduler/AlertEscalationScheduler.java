package backend.scheduler;

import backend.dto.IncidentDto;
import backend.entity.Alert;
import backend.repository.AlertRepository;
import backend.service.IncidentService;
import backend.service.NotificationService;
import backend.service.SettingsService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AlertEscalationScheduler {

    private final AlertRepository alertRepository;
    private final IncidentService incidentService;
    private final SettingsService settingsService;
    private final NotificationService notificationService;

    public AlertEscalationScheduler(AlertRepository alertRepository,
                                    IncidentService incidentService,
                                    SettingsService settingsService,
                                    NotificationService notificationService) {
        this.alertRepository = alertRepository;
        this.incidentService = incidentService;
        this.settingsService = settingsService;
        this.notificationService = notificationService;
    }

    @Scheduled(cron = "0 * * * * *") // Every minute
    public void checkAndEscalateAlerts() {
        try {
            boolean autoEscalate = Boolean.parseBoolean(settingsService.getValue("AUTO_CREATE_INCIDENT", "true"));
            if (!autoEscalate) {
                return;
            }

            int escalationMinutes = Integer.parseInt(settingsService.getValue("ALERT_ESCALATION_MINUTES", "10"));
            LocalDateTime threshold = LocalDateTime.now().minusMinutes(escalationMinutes);

            List<Alert> unescalatedCriticals = alertRepository.findByStatusAndSeverityAndEscalatedFalse("Open", "Critical");

            for (Alert alert : unescalatedCriticals) {
                LocalDateTime alertTime = alert.getLastOccurred() != null ? alert.getLastOccurred() : LocalDateTime.now();
                if (alertTime.isBefore(threshold)) {
                    // Escalate to Incident
                    IncidentDto incidentDto = IncidentDto.builder()
                            .title("[Auto Escalated Alert] " + alert.getTitle())
                            .description("This incident was automatically escalated from alert: " + alert.getDescription() + 
                                    "\nSource: " + alert.getSource() + 
                                    "\nSeverity: " + alert.getSeverity() + 
                                    "\nAlert Occurrence Count: " + alert.getOccurrenceCount())
                            .severity("Critical")
                            .priority("P1")
                            .status("Open")
                            .source(alert.getSource())
                            .build();

                    incidentService.createIncident(incidentDto);

                    // Mark alert as escalated
                    alert.setEscalated(true);
                    alertRepository.save(alert);

                    // Dispatch notification
                    notificationService.saveNotification(
                            "Alert Escalated to Incident",
                            "Critical",
                            "Alert '" + alert.getTitle() + "' has been automatically escalated to a P1 Incident due to no response within " + escalationMinutes + " minutes."
                    );

                    System.out.println("Alert ID " + alert.getId() + " has been auto-escalated to a P1 Incident.");
                }
            }
        } catch (Exception e) {
            System.err.println("Error in Alert Escalation Scheduler: " + e.getMessage());
        }
    }
}
