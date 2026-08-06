package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.Alert;
import backend.entity.AlertRule;
import backend.repository.AlertRepository;
import backend.repository.AlertRuleRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AlertEngineService {

    private final AlertRuleRepository alertRuleRepository;
    private final AlertRepository alertRepository;
    private final AlertNotificationService alertNotificationService;
    private final AIAlertAnalysisService aiAlertAnalysisService;

    public AlertEngineService(
            AlertRuleRepository alertRuleRepository,
            AlertRepository alertRepository,
            AlertNotificationService alertNotificationService,
            AIAlertAnalysisService aiAlertAnalysisService
    ) {

        this.alertRuleRepository = alertRuleRepository;
        this.alertRepository = alertRepository;
        this.alertNotificationService = alertNotificationService;
        this.aiAlertAnalysisService = aiAlertAnalysisService;
    }

    public void processEvent(SecurityEvent event) {

        System.out.println(
                "\n========== ALERT ENGINE STARTED =========="
        );

        System.out.println(
                "Incoming Event Type : " + event.getEventType()
        );

        System.out.println(
                "Incoming Value      : " + event.getValue()
        );

        System.out.println(
                "Incoming Source     : " + event.getSource()
        );

        System.out.println(
                "Incoming Source IP  : " + event.getSourceIp()
        );


        /*
         * Load enabled alert rules
         */
        List<AlertRule> rules =
                alertRuleRepository.findByEnabled(true);

        System.out.println(
                "Enabled Rules Found : " + rules.size()
        );


        for (AlertRule rule : rules) {

            System.out.println("\n-------------------------------");
            System.out.println("Checking Rule");
            System.out.println("Rule Name       : " + rule.getName());
            System.out.println("Rule Event Type : " + rule.getEventType());
            System.out.println("Condition       : " + rule.getConditionType());
            System.out.println("Threshold       : " + rule.getThreshold());


            /*
             * Check Event Type
             */
            if (!rule.getEventType()
                    .equalsIgnoreCase(event.getEventType())) {

                System.out.println(
                        "Event Type NOT Matched"
                );

                continue;
            }

            System.out.println(
                    "Event Type Matched"
            );


            /*
             * Check Rule Condition
             */
            boolean matched = false;

            if (event.getValue() == null ||
                    rule.getThreshold() == null) {

                System.out.println(
                        "Event value or threshold is null."
                );

                continue;
            }


            switch (
                    rule.getConditionType().toUpperCase()
            ) {

                case "GREATER_THAN":

                    matched =
                            event.getValue()
                                    > rule.getThreshold();

                    break;


                case "LESS_THAN":

                    matched =
                            event.getValue()
                                    < rule.getThreshold();

                    break;


                case "EQUAL":

                    matched =
                            event.getValue()
                                    .equals(rule.getThreshold());

                    break;


                default:

                    System.out.println(
                            "Unknown Condition Type"
                    );
            }


            System.out.println(
                    "Condition Matched : " + matched
            );


            if (!matched) {

                System.out.println(
                        "Rule did not match. Alert not created."
                );

                continue;
            }


            /*
             * Check Existing Open Alert
             */
            Optional<Alert> existingAlert =
                    alertRepository.findByTitleAndStatus(
                            rule.getName(),
                            "Open"
                    );


            /*
             * ========================================
             * EXISTING ALERT
             * ========================================
             */
            if (existingAlert.isPresent()) {

                Alert alert =
                        existingAlert.get();

                alert.setSeverity(
                        rule.getSeverity()
                );

                alert.setSource(
                        event.getSource()
                );

                alert.setDescription(
                        event.getDescription()
                );


                if (alert.getOccurrenceCount() == null) {

                    alert.setOccurrenceCount(0);
                }


                alert.setOccurrenceCount(
                        alert.getOccurrenceCount() + 1
                );


                alert.setLastOccurred(
                        LocalDateTime.now()
                );


                Alert updatedAlert =
                        alertRepository.save(alert);


                /*
                 * Send notification for every occurrence
                 */
                alertNotificationService
                        .sendNotification(updatedAlert);


                System.out.println(
                        "Existing Alert Updated!"
                );

                System.out.println(
                        "Alert ID : "
                                + updatedAlert.getId()
                );

                System.out.println(
                        "Occurrence Count : "
                                + updatedAlert.getOccurrenceCount()
                );


                /*
                 * IMPORTANT:
                 *
                 * We DO NOT call AI again here.
                 *
                 * Existing alert already has AI analysis.
                 * This prevents unnecessary Ollama requests.
                 */

            }


            /*
             * ========================================
             * NEW ALERT
             * ========================================
             */
            else {

                Alert alert = new Alert();

                alert.setTitle(
                        rule.getName()
                );

                alert.setSeverity(
                        rule.getSeverity()
                );

                alert.setSource(
                        event.getSource()
                );

                alert.setStatus(
                        "Open"
                );

                alert.setDescription(
                        event.getDescription()
                );

                alert.setOccurrenceCount(1);

                alert.setLastOccurred(
                        LocalDateTime.now()
                );


                /*
                 * Save Alert First
                 */
                Alert savedAlert =
                        alertRepository.save(alert);


                System.out.println(
                        "New Alert Created!"
                );

                System.out.println(
                        "Alert ID : "
                                + savedAlert.getId()
                );

                System.out.println(
                        "Severity : "
                                + savedAlert.getSeverity()
                );


                /*
                 * ========================================
                 * AUTOMATIC AI ANALYSIS
                 * ========================================
                 *
                 * Only HIGH and CRITICAL alerts
                 * are analyzed.
                 */
                if (isHighRisk(
                        savedAlert.getSeverity()
                )) {

                    System.out.println(
                            "\n========== AI ANALYSIS STARTED =========="
                    );


                    String analysis =
                            aiAlertAnalysisService.analyze(
                                    savedAlert,
                                    event.getSourceIp()
                            );


                    if (analysis != null &&
                            !analysis.trim().isEmpty()) {

                        savedAlert.setAiAnalysis(
                                analysis
                        );


                        savedAlert =
                                alertRepository.save(
                                        savedAlert
                                );


                        System.out.println(
                                "AI Analysis saved successfully."
                        );

                    } else {

                        System.out.println(
                                "AI did not return analysis."
                        );
                    }


                    System.out.println(
                            "========== AI ANALYSIS FINISHED =========="
                    );

                } else {

                    System.out.println(
                            "AI analysis skipped for severity: "
                                    + savedAlert.getSeverity()
                    );
                }


                /*
                 * Send Real-Time Notification
                 */
                alertNotificationService
                        .sendNotification(savedAlert);


                System.out.println(
                        "Notification sent successfully."
                );

                System.out.println(
                        "Occurrence Count : "
                                + savedAlert.getOccurrenceCount()
                );
            }
        }


        System.out.println(
                "========== ALERT ENGINE FINISHED ==========\n"
        );
    }


    /*
     * Determine whether AI analysis is required
     */
    private boolean isHighRisk(String severity) {

        if (severity == null) {
            return false;
        }

        return severity.equalsIgnoreCase("HIGH")
                || severity.equalsIgnoreCase("CRITICAL");
    }
}