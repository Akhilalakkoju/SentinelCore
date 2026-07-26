package backend.controller;

import backend.dto.*;
import backend.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/settings")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    // ==========================================
    // ALERT SETTINGS
    // ==========================================
    @GetMapping("/alerts")
    public ResponseEntity<AlertSettingsDTO> getAlertSettings() {
        return ResponseEntity.ok(settingsService.getAlertSettings());
    }

    @PutMapping("/alerts")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlertSettingsDTO> updateAlertSettings(Authentication authentication,
                                                                 @RequestBody AlertSettingsDTO dto) {
        String updatedBy = authentication != null ? authentication.getName() : "ADMIN";
        return ResponseEntity.ok(settingsService.updateAlertSettings(dto, updatedBy));
    }

    // ==========================================
    // NOTIFICATION SETTINGS
    // ==========================================
    @GetMapping("/notifications")
    public ResponseEntity<NotificationSettingsDTO> getNotificationSettings() {
        return ResponseEntity.ok(settingsService.getNotificationSettings());
    }

    @PutMapping("/notifications")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<NotificationSettingsDTO> updateNotificationSettings(Authentication authentication,
                                                                                @RequestBody NotificationSettingsDTO dto) {
        String updatedBy = authentication != null ? authentication.getName() : "ADMIN";
        return ResponseEntity.ok(settingsService.updateNotificationSettings(dto, updatedBy));
    }

    // ==========================================
    // DATA RETENTION SETTINGS
    // ==========================================
    @GetMapping("/retention")
    public ResponseEntity<RetentionSettingsDTO> getRetentionSettings() {
        return ResponseEntity.ok(settingsService.getRetentionSettings());
    }

    @PutMapping("/retention")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RetentionSettingsDTO> updateRetentionSettings(Authentication authentication,
                                                                         @RequestBody RetentionSettingsDTO dto) {
        String updatedBy = authentication != null ? authentication.getName() : "ADMIN";
        return ResponseEntity.ok(settingsService.updateRetentionSettings(dto, updatedBy));
    }

    // ==========================================
    // SYSTEM SETTINGS
    // ==========================================
    @GetMapping("/system")
    public ResponseEntity<SystemSectionSettingsDTO> getSystemSettings() {
        return ResponseEntity.ok(settingsService.getSystemSettings());
    }

    @PutMapping("/system")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSectionSettingsDTO> updateSystemSettings(Authentication authentication,
                                                                         @RequestBody SystemSectionSettingsDTO dto) {
        String updatedBy = authentication != null ? authentication.getName() : "ADMIN";
        return ResponseEntity.ok(settingsService.updateSystemSettings(dto, updatedBy));
    }
}
