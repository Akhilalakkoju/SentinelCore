package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.IOC;
import backend.entity.Threat;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SecurityEventProcessingService {

    private final IOCDetectionService iocDetectionService;
    private final ThreatDetectionService threatDetectionService;
    private final AlertEngineService alertEngineService;

    public SecurityEventProcessingService(
            IOCDetectionService iocDetectionService,
            ThreatDetectionService threatDetectionService,
            AlertEngineService alertEngineService
    ) {
        this.iocDetectionService = iocDetectionService;
        this.threatDetectionService = threatDetectionService;
        this.alertEngineService = alertEngineService;
    }

    public void processSecurityEvent(SecurityEvent event) {

        System.out.println(
                "\n=============================================="
        );
        System.out.println(
                "       SENTINELCORE EVENT PROCESSOR"
        );
        System.out.println(
                "=============================================="
        );

        if (event == null) {
            System.out.println("Security event is null.");
            return;
        }

        System.out.println(
                "Event Type     : " + event.getEventType()
        );

        System.out.println(
                "Value          : " + event.getValue()
        );

        System.out.println(
                "Source         : " + event.getSource()
        );

        System.out.println(
                "Indicator      : " + event.getIndicator()
        );

        System.out.println(
                "Indicator Type : " + event.getIndicatorType()
        );

        // ==========================================
        // STEP 1: IOC DETECTION
        // ==========================================

        if (
                event.getIndicator() != null &&
                        !event.getIndicator().isBlank()
        ) {

            Optional<IOC> matchedIOC =
                    iocDetectionService.detectIOC(
                            event.getIndicator()
                    );

            if (matchedIOC.isPresent()) {

                IOC ioc = matchedIOC.get();

                if (isActiveIOC(ioc)) {

                    System.out.println(
                            "🚨 ACTIVE IOC MATCH DETECTED"
                    );

                    // ==================================
                    // STEP 2: CREATE / UPDATE THREAT
                    // ==================================

                    Threat threat =
                            threatDetectionService
                                    .createThreatFromIOC(
                                            ioc,
                                            event
                                    );

                    System.out.println(
                            "Threat processed successfully."
                    );

                    System.out.println(
                            "Threat ID : " + threat.getId()
                    );

                    // ==================================
                    // STEP 3:
                    // Convert IOC match into an event
                    // that Alert Engine can understand.
                    // ==================================

                    SecurityEvent iocAlertEvent =
                            createIOCAlertEvent(
                                    event,
                                    ioc
                            );

                    // ==================================
                    // STEP 4: ALERT ENGINE
                    // ==================================

                    alertEngineService.processEvent(
                            iocAlertEvent
                    );

                } else {

                    System.out.println(
                            "IOC exists but is inactive."
                    );
                }

            } else {

                System.out.println(
                        "No IOC match detected."
                );
            }
        } else {

            System.out.println(
                    "No IOC indicator supplied."
            );
        }

        // ==========================================
        // STEP 5:
        // Process original event through normal
        // alert rules.
        // ==========================================

        alertEngineService.processEvent(event);

        System.out.println(
                "=============================================="
        );

        System.out.println(
                "      EVENT PROCESSING COMPLETED"
        );

        System.out.println(
                "==============================================\n"
        );
    }

    private SecurityEvent createIOCAlertEvent(
            SecurityEvent originalEvent,
            IOC ioc
    ) {

        SecurityEvent iocEvent =
                new SecurityEvent();

        /*
         * We'll create an Alert Rule with this
         * Event Type in the next step.
         */
        iocEvent.setEventType("IOC_MATCH");

        iocEvent.setValue(1);

        iocEvent.setSource(
                originalEvent.getSource() != null
                        ? originalEvent.getSource()
                        : "IOC Detection Engine"
        );

        iocEvent.setDescription(
                "Malicious IOC detected. " +
                        "Type: " + ioc.getType() +
                        ", Value: " + ioc.getValue() +
                        ", Risk Level: " + ioc.getRiskLevel()
        );

        iocEvent.setIndicator(
                ioc.getValue()
        );

        iocEvent.setIndicatorType(
                ioc.getType()
        );

        return iocEvent;
    }

    private boolean isActiveIOC(IOC ioc) {

        if (ioc.getStatus() == null) {
            return true;
        }

        String status =
                ioc.getStatus()
                        .trim()
                        .toUpperCase();

        return !status.equals("INACTIVE")
                && !status.equals("DISABLED")
                && !status.equals("RESOLVED");
    }
}