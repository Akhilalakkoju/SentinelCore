package backend.controller;

import backend.entity.Threat;
import backend.entity.Incident;
import backend.entity.PlaybookExecution;
import backend.repository.AlertRepository;
import backend.repository.ThreatRepository;
import backend.repository.IncidentRepository;
import backend.repository.AssetDiskRepository;
import backend.repository.PlaybookExecutionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class DashboardController {

    @Autowired
    private ThreatRepository threatRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private AssetDiskRepository assetDiskRepository;

    @Autowired
    private PlaybookExecutionRepository playbookExecutionRepository;

    // ================= Dashboard Cards =================

    @GetMapping("/stats")
    public Map<String, Object> getStats() {

        Map<String, Object> stats = new HashMap<>();

        // Threat Statistics
        stats.put("totalThreats", threatRepository.count());
        stats.put("criticalThreats", threatRepository.countBySeverity("Critical"));
        stats.put("openThreats", threatRepository.countByStatus("Open"));
        stats.put("resolvedThreats", threatRepository.countByStatus("Resolved"));

        // Alert Statistics
        stats.put("totalAlerts", alertRepository.count());
        stats.put("criticalAlerts", alertRepository.countBySeverity("Critical"));
        stats.put("openAlerts", alertRepository.countByStatus("Open"));
        stats.put("resolvedAlerts", alertRepository.countByStatus("Resolved"));

        return stats;
    }

    // ================= Threat Distribution =================

    @GetMapping("/chart")
    public List<Map<String, Object>> getChartData() {

        List<Map<String, Object>> chart = new ArrayList<>();

        chart.add(Map.of(
                "severity", "Critical",
                "count", threatRepository.countBySeverity("Critical")
        ));

        chart.add(Map.of(
                "severity", "High",
                "count", threatRepository.countBySeverity("High")
        ));

        chart.add(Map.of(
                "severity", "Medium",
                "count", threatRepository.countBySeverity("Medium")
        ));

        chart.add(Map.of(
                "severity", "Low",
                "count", threatRepository.countBySeverity("Low")
        ));

        return chart;
    }

    // ================= Recent Threats =================

    @GetMapping("/recent-threats")
    public List<Threat> getRecentThreats() {

        return threatRepository.findTop5ByOrderByIdDesc();

    }

    // ================= Low Disk Space Playbook Stats =================

    @GetMapping("/low-disk-stats")
    public Map<String, Object> getLowDiskStats() {
        Map<String, Object> stats = new HashMap<>();
        
        // Total Low Disk Space Incidents
        long totalIncidents = incidentRepository.findAll().stream()
                .filter(i -> "Low Disk Space Detected".equals(i.getTitle()))
                .count();
                
        // Active Incidents (Low Disk Space)
        long activeIncidents = incidentRepository.findAll().stream()
                .filter(i -> "Low Disk Space Detected".equals(i.getTitle()) 
                        && ("Open".equalsIgnoreCase(i.getStatus()) || "Investigating".equalsIgnoreCase(i.getStatus())))
                .count();
                
        // Resolved Incidents (Low Disk Space)
        long resolvedIncidents = incidentRepository.findAll().stream()
                .filter(i -> "Low Disk Space Detected".equals(i.getTitle()) 
                        && "Resolved".equalsIgnoreCase(i.getStatus()))
                .count();
        
        // Assets with Critical Disk Usage (disk usage percentage >= 90.0)
        long criticalDiskAssets = assetDiskRepository.findAll().stream()
                .filter(d -> d.getDiskUsagePercentage() != null && d.getDiskUsagePercentage() >= 90.0)
                .map(d -> d.getAsset().getId())
                .distinct()
                .count();

        // Recent Playbook Executions (for Low Disk Space Playbook)
        List<PlaybookExecution> recentExecutions = playbookExecutionRepository.findAll().stream()
                .filter(e -> "Low Disk Space Playbook".equals(e.getPlaybookName()))
                .sorted((e1, e2) -> e2.getId().compareTo(e1.getId()))
                .limit(5)
                .collect(Collectors.toList());

        stats.put("totalLowDiskIncidents", totalIncidents);
        stats.put("activeLowDiskIncidents", activeIncidents);
        stats.put("resolvedLowDiskIncidents", resolvedIncidents);
        stats.put("criticalDiskAssetsCount", criticalDiskAssets);
        stats.put("recentExecutions", recentExecutions.stream().map(e -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", e.getId());
            map.put("playbookName", e.getPlaybookName());
            map.put("status", e.getStatus());
            map.put("progress", e.getProgress());
            map.put("startedAt", e.getStartedAt() != null ? e.getStartedAt().toString() : null);
            map.put("endedAt", e.getEndedAt() != null ? e.getEndedAt().toString() : null);
            map.put("incidentTitle", e.getIncident() != null ? e.getIncident().getTitle() : null);
            map.put("incidentId", e.getIncidentId());
            return map;
        }).collect(Collectors.toList()));

        return stats;
    }

}