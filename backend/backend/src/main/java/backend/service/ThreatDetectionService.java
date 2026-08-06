package backend.service;

import backend.dto.SecurityEvent;
import backend.entity.IOC;
import backend.entity.Threat;
import backend.repository.ThreatRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ThreatDetectionService {

    private final ThreatRepository threatRepository;

    public ThreatDetectionService(
            ThreatRepository threatRepository
    ) {
        this.threatRepository = threatRepository;
    }

    public Threat createThreatFromIOC(
            IOC ioc,
            SecurityEvent event
    ) {

        System.out.println(
                "\n========== THREAT DETECTION STARTED =========="
        );

        String title =
                "Malicious " +
                        normalizeType(ioc.getType()) +
                        " Detected";

        String status = "Open";

        System.out.println("Threat Title : " + title);
        System.out.println("IOC Value    : " + ioc.getValue());
        System.out.println("IOC Type     : " + ioc.getType());
        System.out.println("Risk Level   : " + ioc.getRiskLevel());

        Optional<Threat> existingThreat =
                threatRepository.findByTitleAndStatus(
                        title,
                        status
                );

        if (existingThreat.isPresent()) {

            Threat threat = existingThreat.get();

            /*
             * Refresh severity/source using the latest
             * IOC/event information.
             */
            threat.setSeverity(
                    normalizeSeverity(ioc.getRiskLevel())
            );

            threat.setSource(
                    buildSource(ioc, event)
            );

            threatRepository.save(threat);

            System.out.println(
                    "Existing Threat Found!"
            );

            System.out.println(
                    "Threat ID : " + threat.getId()
            );

            System.out.println(
                    "========== THREAT DETECTION FINISHED ==========\n"
            );

            return threat;
        }

        Threat threat = new Threat();

        threat.setTitle(title);

        threat.setSeverity(
                normalizeSeverity(ioc.getRiskLevel())
        );

        threat.setSource(
                buildSource(ioc, event)
        );

        threat.setStatus(status);

        Threat savedThreat =
                threatRepository.save(threat);

        System.out.println(
                "🚨 NEW THREAT CREATED"
        );

        System.out.println(
                "Threat ID       : " + savedThreat.getId()
        );

        System.out.println(
                "Threat Title    : " + savedThreat.getTitle()
        );

        System.out.println(
                "Threat Severity : " + savedThreat.getSeverity()
        );

        System.out.println(
                "Threat Source   : " + savedThreat.getSource()
        );

        System.out.println(
                "========== THREAT DETECTION FINISHED ==========\n"
        );

        return savedThreat;
    }

    private String normalizeType(String type) {

        if (type == null || type.isBlank()) {
            return "IOC";
        }

        return type.trim().toUpperCase();
    }

    private String normalizeSeverity(String riskLevel) {

        if (riskLevel == null || riskLevel.isBlank()) {
            return "MEDIUM";
        }

        String severity =
                riskLevel.trim().toUpperCase();

        return switch (severity) {

            case "CRITICAL" -> "CRITICAL";

            case "HIGH" -> "HIGH";

            case "MEDIUM" -> "MEDIUM";

            case "LOW" -> "LOW";

            default -> "MEDIUM";
        };
    }

    private String buildSource(
            IOC ioc,
            SecurityEvent event
    ) {

        if (
                event.getSource() != null &&
                        !event.getSource().isBlank()
        ) {
            return event.getSource();
        }

        if (
                ioc.getSource() != null &&
                        !ioc.getSource().isBlank()
        ) {
            return ioc.getSource();
        }

        return "IOC Detection Engine";
    }
}