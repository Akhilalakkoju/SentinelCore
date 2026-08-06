package backend.service;

import backend.entity.Alert;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AIAlertAnalysisService {

    private static final String AI_URL =
            "http://localhost:8000/analyze-alert";

    private final RestTemplate restTemplate = new RestTemplate();

    public String analyze(Alert alert, String sourceIp) {

        try {

            Map<String, String> request = new HashMap<>();

            request.put("title", alert.getTitle());
            request.put("severity", alert.getSeverity());
            request.put("description",
                    alert.getDescription() != null
                            ? alert.getDescription()
                            : "");

            request.put("sourceIp",
                    sourceIp != null ? sourceIp : "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity =
                    new HttpEntity<>(request, headers);

            ResponseEntity<Map> response =
                    restTemplate.postForEntity(
                            AI_URL,
                            entity,
                            Map.class
                    );

            if (response.getBody() != null) {

                Object analysis =
                        response.getBody().get("analysis");

                if (analysis != null) {

                    System.out.println(
                            "🤖 AI ALERT ANALYSIS COMPLETED"
                    );

                    return analysis.toString();
                }
            }

        } catch (Exception e) {

            System.err.println(
                    "AI Analysis Failed: " + e.getMessage()
            );
        }

        return null;
    }
}