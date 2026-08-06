package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.IOC;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventIngestionService {

    private final IOCCorrelationService iocCorrelationService;
    private final AlertEngineService alertEngineService;

    public EventIngestionService(
            IOCCorrelationService iocCorrelationService,
            AlertEngineService alertEngineService
    ) {
        this.iocCorrelationService = iocCorrelationService;
        this.alertEngineService = alertEngineService;
    }

    public void ingest(SecurityEvent event) {

        System.out.println(
                "\n========================================"
        );

        System.out.println(
                "       SENTINELCORE EVENT INGESTION"
        );

        System.out.println(
                "========================================"
        );

        validateEvent(event);

        normalizeEvent(event);

        System.out.println(
                "Event Type  : " + event.getEventType()
        );

        System.out.println(
                "Source      : " + event.getSource()
        );

        System.out.println(
                "Description : " + event.getDescription()
        );

        /*
         * STEP 1
         * IOC CORRELATION
         */

        Optional<IOC> matchedIOC =
                iocCorrelationService.checkIOC(event);

        if (matchedIOC.isPresent()) {

            IOC ioc = matchedIOC.get();

            System.out.println(
                    "🚨 MALICIOUS IOC DETECTED"
            );

            /*
             * Convert the IOC detection into a
             * normalized security event.
             */

            SecurityEvent iocEvent =
                    new SecurityEvent();

            iocEvent.setEventType("IOC_MATCH");

            iocEvent.setValue(1);

            iocEvent.setSource(
                    event.getSource()
            );

            iocEvent.setDescription(
                    "Malicious IOC detected. " +
                            "Type: " + ioc.getType() +
                            ", Value: " + ioc.getValue() +
                            ", Risk: " + ioc.getRiskLevel()
            );

            iocEvent.setIndicator(
                    ioc.getValue()
            );

            iocEvent.setIndicatorType(
                    ioc.getType()
            );

            iocEvent.setAssetName(
                    event.getAssetName()
            );

            iocEvent.setSourceIp(
                    event.getSourceIp()
            );

            iocEvent.setDestinationIp(
                    event.getDestinationIp()
            );

            /*
             * Send IOC_MATCH to Alert Engine
             */

            alertEngineService.processEvent(
                    iocEvent
            );

        } else {

            /*
             * No IOC match.
             *
             * Still process the original event
             * because FAILED_LOGIN, PORT_SCAN,
             * DDOS etc. may match Alert Rules.
             */

            alertEngineService.processEvent(
                    event
            );
        }

        System.out.println(
                "========== INGESTION COMPLETE ==========\n"
        );
    }


    private void validateEvent(SecurityEvent event) {

        if (event == null) {
            throw new IllegalArgumentException(
                    "Security event cannot be null"
            );
        }

        if (event.getEventType() == null ||
                event.getEventType().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Event type is required"
            );
        }

        if (event.getValue() == null) {
            event.setValue(1);
        }

        if (event.getSource() == null ||
                event.getSource().trim().isEmpty()) {

            event.setSource("Unknown");
        }
    }


    private void normalizeEvent(SecurityEvent event) {

        event.setEventType(
                event.getEventType()
                        .trim()
                        .toUpperCase()
        );

        if (event.getIndicatorType() != null) {

            event.setIndicatorType(
                    event.getIndicatorType()
                            .trim()
                            .toUpperCase()
            );
        }

        if (event.getIndicator() != null) {

            event.setIndicator(
                    event.getIndicator().trim()
            );
        }
    }
}