package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.IOC;
import backend.repository.IOCRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class IOCDetectionService {

    private final IOCRepository iocRepository;

    public IOCDetectionService(IOCRepository iocRepository) {
        this.iocRepository = iocRepository;
    }

    /**
     * Detect IOC by value.
     */
    public Optional<IOC> detectIOC(String value) {

        System.out.println("\n========== IOC DETECTION STARTED ==========");

        if (value == null || value.trim().isEmpty()) {

            System.out.println("IOC value is empty.");
            System.out.println("========== IOC DETECTION FINISHED ==========\n");

            return Optional.empty();
        }

        String normalizedValue = value.trim();

        System.out.println("Checking IOC Value : " + normalizedValue);

        Optional<IOC> matchedIOC =
                iocRepository.findByValue(normalizedValue);

        if (matchedIOC.isPresent()) {

            IOC ioc = matchedIOC.get();

            System.out.println("🚨 IOC MATCH FOUND");
            System.out.println("IOC ID         : " + ioc.getId());
            System.out.println("IOC Type       : " + ioc.getType());
            System.out.println("IOC Value      : " + ioc.getValue());
            System.out.println("Risk Level     : " + ioc.getRiskLevel());
            System.out.println("Source         : " + ioc.getSource());
            System.out.println("Status         : " + ioc.getStatus());

        } else {

            System.out.println("No IOC match found.");

        }

        System.out.println("========== IOC DETECTION FINISHED ==========\n");

        return matchedIOC;
    }

    /**
     * Check whether the IOC is active.
     */
    public boolean isMaliciousIOC(String value) {

        Optional<IOC> matchedIOC = detectIOC(value);

        if (matchedIOC.isEmpty()) {
            return false;
        }

        IOC ioc = matchedIOC.get();

        if (ioc.getStatus() != null) {

            String status =
                    ioc.getStatus().trim().toUpperCase();

            if (status.equals("INACTIVE")
                    || status.equals("DISABLED")
                    || status.equals("RESOLVED")) {

                System.out.println("IOC exists but is inactive.");

                return false;
            }
        }

        return true;
    }

    /**
     * Enrich SecurityEvent with IOC information.
     */
    public Optional<IOC> analyzeEvent(SecurityEvent event) {

        if (event == null) {
            return Optional.empty();
        }

        Optional<IOC> matchedIOC =
                detectIOC(event.getSourceIp());

        if (matchedIOC.isPresent()) {

            IOC ioc = matchedIOC.get();

            event.setIndicator(ioc.getValue());
            event.setIndicatorType(ioc.getType());

            System.out.println("\n========== EVENT ENRICHED ==========");
            System.out.println("Indicator      : " + event.getIndicator());
            System.out.println("Indicator Type : " + event.getIndicatorType());

        }

        return matchedIOC;
    }

}