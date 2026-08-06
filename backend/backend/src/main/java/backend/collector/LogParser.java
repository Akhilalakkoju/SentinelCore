package backend.collector;

import backend.dto.SecurityEvent;
import org.springframework.stereotype.Component;

@Component
public class LogParser {

    public SecurityEvent parse(String logLine) {

        try {

            if (logLine == null || logLine.trim().isEmpty()) {
                return null;
            }

            String[] parts = logLine.trim().split("\\s+");

            /*
             Expected Log Format

             2026-08-06 00:30:00 FAILED_LOGIN Firewall 192.168.1.10

             parts[0] = Date
             parts[1] = Time
             parts[2] = Event Type
             parts[3] = Source
             parts[4] = Source IP
             */

            if (parts.length < 5) {

                System.out.println("Invalid Log Format : " + logLine);
                return null;

            }

            SecurityEvent event = new SecurityEvent();

            event.setEventType(parts[2].toUpperCase());
            event.setSource(parts[3]);
            event.setSourceIp(parts[4]);
            event.setDescription(logLine);

            switch (event.getEventType()) {

                case "FAILED_LOGIN":
                    event.setValue(8);
                    break;

                case "BRUTE_FORCE":
                    event.setValue(15);
                    break;

                case "PORT_SCAN":
                    event.setValue(12);
                    break;

                case "MALWARE":
                case "MALWARE_DETECTED":
                    event.setValue(20);
                    break;

                case "RANSOMWARE":
                    event.setValue(25);
                    break;

                case "SQL_INJECTION":
                    event.setValue(18);
                    break;

                case "XSS_ATTACK":
                    event.setValue(10);
                    break;

                case "UNAUTHORIZED_ACCESS":
                    event.setValue(18);
                    break;

                case "PRIVILEGE_ESCALATION":
                    event.setValue(22);
                    break;

                case "DDOS_ATTACK":
                    event.setValue(30);
                    break;

                default:
                    event.setValue(1);
            }

            System.out.println("\n========== EVENT PARSED ==========");
            System.out.println("Event Type      : " + event.getEventType());
            System.out.println("Source          : " + event.getSource());
            System.out.println("Source IP       : " + event.getSourceIp());
            System.out.println("Risk Value      : " + event.getValue());
            System.out.println("Description     : " + event.getDescription());

            return event;

        } catch (Exception e) {

            System.out.println("Parser Error : " + e.getMessage());
            e.printStackTrace();

            return null;
        }
    }
}