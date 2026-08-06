package backend.controller;

import backend.dto.SecurityEvent;
import backend.service.SecurityEventProcessingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/alert-engine")
public class AlertEngineController {

    private final SecurityEventProcessingService
            securityEventProcessingService;

    public AlertEngineController(
            SecurityEventProcessingService
                    securityEventProcessingService
    ) {
        this.securityEventProcessingService =
                securityEventProcessingService;
    }

    @PostMapping("/process")
    public ResponseEntity<String> processEvent(
            @RequestBody SecurityEvent event
    ) {

        securityEventProcessingService
                .processSecurityEvent(event);

        return ResponseEntity.ok(
                "Security event processed successfully."
        );
    }
}