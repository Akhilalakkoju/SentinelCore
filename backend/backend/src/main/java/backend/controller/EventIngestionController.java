package backend.controller;

import backend.dto.SecurityEvent;
import backend.service.EventIngestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/events")
public class EventIngestionController {

    private final EventIngestionService eventIngestionService;

    public EventIngestionController(
            EventIngestionService eventIngestionService
    ) {
        this.eventIngestionService =
                eventIngestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<String> ingestEvent(
            @RequestBody SecurityEvent event
    ) {

        try {

            eventIngestionService.ingest(event);

            return ResponseEntity.ok(
                    "Security event ingested successfully."
            );

        } catch (IllegalArgumentException e) {

            return ResponseEntity
                    .badRequest()
                    .body(e.getMessage());

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity
                    .internalServerError()
                    .body(
                            "Failed to process security event."
                    );
        }
    }
}