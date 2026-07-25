package backend.service;

import backend.dto.IncidentDto;
import backend.dto.KnowledgeBaseArticleSummaryDto;
import backend.entity.Incident;
import backend.entity.User;
import backend.entity.KnowledgeBaseArticle;
import backend.repository.IncidentRepository;
import backend.repository.UserRepository;
import backend.repository.KnowledgeBaseArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class IncidentService {

    private final IncidentRepository incidentRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final backend.repository.AuditLogRepository auditLogRepository;
    private final backend.repository.PlaybookExecutionRepository playbookExecutionRepository;
    private final backend.repository.PlaybookExecutionLogRepository playbookExecutionLogRepository;
    private final backend.repository.PlaybookNotificationRepository playbookNotificationRepository;
    private final backend.repository.PlaybookAuditLogRepository playbookAuditLogRepository;
    private final KnowledgeBaseArticleRepository kbArticleRepository;
    private final backend.repository.AssetRepository assetRepository;

    // Get All Incidents
    public List<IncidentDto> getAllIncidents() {
        return incidentRepository.findAllByOrderByEscalatedDescIdDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get Incidents by Asset ID
    public List<IncidentDto> getIncidentsByAsset(Long assetId) {
        return incidentRepository.findByAssetId(assetId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    // Get Incident by ID
    public IncidentDto getIncidentById(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));
        return convertToDto(incident);
    }

    // Create Incident
    @Transactional
    public IncidentDto createIncident(IncidentDto dto) {
        Incident incident = convertToEntity(dto);
        incident.setEscalated(false);
        
        LocalDateTime now = LocalDateTime.now();
        incident.setCreatedAt(now);
        incident.setUpdatedAt(now);

        // Compute SLA deadline
        setDeadlineBasedOnPriority(incident, dto.getPriority());

        Incident savedIncident = incidentRepository.save(incident);

        // Audit Log
        auditLogService.createLog(
                "CREATE",
                "Incident created: " + savedIncident.getTitle(),
                getCurrentUser(),
                savedIncident
        );

        return convertToDto(savedIncident);
    }

    // Update Incident
    @Transactional
    public IncidentDto updateIncident(Long id, IncidentDto dto) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        List<String> changes = new ArrayList<>();

        if (dto.getTitle() != null && !dto.getTitle().equals(incident.getTitle())) {
            changes.add("Title changed from '" + incident.getTitle() + "' to '" + dto.getTitle() + "'");
            incident.setTitle(dto.getTitle());
        }

        if (dto.getDescription() != null && !dto.getDescription().equals(incident.getDescription())) {
            changes.add("Description modified");
            incident.setDescription(dto.getDescription());
        }

        if (dto.getSeverity() != null && !dto.getSeverity().equals(incident.getSeverity())) {
            changes.add("Severity changed from '" + incident.getSeverity() + "' to '" + dto.getSeverity() + "'");
            incident.setSeverity(dto.getSeverity());
        }

        if (dto.getStatus() != null && !dto.getStatus().equals(incident.getStatus())) {
            changes.add("Status changed from '" + incident.getStatus() + "' to '" + dto.getStatus() + "'");
            incident.setStatus(dto.getStatus());
        }

        if (dto.getSource() != null && !dto.getSource().equals(incident.getSource())) {
            changes.add("Source changed from '" + incident.getSource() + "' to '" + dto.getSource() + "'");
            incident.setSource(dto.getSource());
        }

        if (dto.getPriority() != null && !dto.getPriority().equals(incident.getPriority())) {
            changes.add("Priority changed from '" + incident.getPriority() + "' to '" + dto.getPriority() + "'");
            incident.setPriority(dto.getPriority());
            setDeadlineBasedOnPriority(incident, dto.getPriority());
        }

        if (dto.getAssignedToId() != null) {
            User user = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getAssignedToId()));
            if (incident.getAssignedTo() == null || !user.getId().equals(incident.getAssignedTo().getId())) {
                String oldAssignee = incident.getAssignedTo() != null ? incident.getAssignedTo().getEmail() : "Unassigned";
                changes.add("Assignee changed from '" + oldAssignee + "' to '" + user.getEmail() + "'");
                incident.setAssignedTo(user);
            }
        } else {
            if (incident.getAssignedTo() != null) {
                changes.add("Assignee changed from '" + incident.getAssignedTo().getEmail() + "' to 'Unassigned'");
                incident.setAssignedTo(null);
            }
        }

        incident.setUpdatedAt(LocalDateTime.now());
        Incident updatedIncident = incidentRepository.save(incident);

        String changeLogStr = changes.isEmpty() ? "No changes" : String.join(", ", changes);
        auditLogService.createLog(
                "UPDATE",
                changeLogStr,
                getCurrentUser(),
                updatedIncident
        );

        return convertToDto(updatedIncident);
    }

    // Delete Incident
    @Transactional
    public void deleteIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        // Create log BEFORE deletion, passing null to the incident association so it's not cascade-deleted
        auditLogService.createLog(
                "DELETE",
                "Incident deleted: " + incident.getTitle() + " (ID: #" + id + ")",
                getCurrentUser(),
                null
        );

        incidentRepository.delete(incident);
    }

    // Escalate Incident
    @Transactional
    public IncidentDto escalateIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        boolean oldEscalated = incident.getEscalated() != null ? incident.getEscalated() : false;
        incident.setEscalated(true);
        incident.setUpdatedAt(LocalDateTime.now());
        Incident saved = incidentRepository.save(incident);

        auditLogService.createLog(
                "UPDATE",
                "Escalated status updated from " + oldEscalated + " to true",
                getCurrentUser(),
                saved
        );

        return convertToDto(saved);
    }

    // Resolve Incident
    @Transactional
    public IncidentDto resolveIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        String oldStatus = incident.getStatus();
        incident.setStatus("Resolved");
        incident.setUpdatedAt(LocalDateTime.now());
        Incident saved = incidentRepository.save(incident);

        auditLogService.createLog(
                "UPDATE",
                "Status changed from " + oldStatus + " to Resolved",
                getCurrentUser(),
                saved
        );

        return convertToDto(saved);
    }

    // Remaining SLA helper
    public String getRemainingSLA(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        if (incident.getSlaDeadline() == null) {
            return "No SLA";
        }

        Duration duration = Duration.between(LocalDateTime.now(), incident.getSlaDeadline());

        if (duration.isNegative()) {
            return "SLA BREACHED";
        }

        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        return hours + "h " + minutes + "m";
    }

    // SLA Breached helper
    public boolean isSLABreached(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + id));

        if (incident.getSlaDeadline() == null) {
            return false;
        }

        return LocalDateTime.now().isAfter(incident.getSlaDeadline());
    }

    private void setDeadlineBasedOnPriority(Incident incident, String priority) {
        LocalDateTime now = LocalDateTime.now();
        if (priority == null) {
            incident.setSlaDeadline(now.plusHours(24));
            return;
        }
        switch (priority.toUpperCase()) {
            case "P1":
                incident.setSlaDeadline(now.plusHours(2));
                break;
            case "P2":
                incident.setSlaDeadline(now.plusHours(4));
                break;
            case "P3":
                incident.setSlaDeadline(now.plusHours(8));
                break;
            default:
                incident.setSlaDeadline(now.plusHours(24));
        }
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    // Entity to DTO mapper
    public IncidentDto convertToDto(Incident incident) {
        IncidentDto.IncidentDtoBuilder builder = IncidentDto.builder()
                .id(incident.getId())
                .title(incident.getTitle())
                .description(incident.getDescription())
                .severity(incident.getSeverity())
                .status(incident.getStatus())
                .source(incident.getSource())
                .priority(incident.getPriority())
                .escalated(incident.getEscalated())
                .slaDeadline(incident.getSlaDeadline())
                .createdAt(incident.getCreatedAt())
                .updatedAt(incident.getUpdatedAt());

        if (incident.getAssignedTo() != null) {
            builder.assignedToId(incident.getAssignedTo().getId())
                   .assignedToName(incident.getAssignedTo().getName());
        }

        if (incident.getKbArticles() != null) {
            builder.linkedArticles(incident.getKbArticles().stream()
                    .map(art -> KnowledgeBaseArticleSummaryDto.builder()
                            .id(art.getId())
                            .title(art.getTitle())
                            .type(art.getType())
                            .version(art.getVersion())
                            .build())
                    .collect(Collectors.toList()));
        } else {
            builder.linkedArticles(new ArrayList<>());
        }

        if (incident.getAsset() != null) {
            builder.assetId(incident.getAsset().getId())
                   .assetName(incident.getAsset().getAssetName());
        }

        return builder.build();
    }

    // DTO to Entity mapper
    private Incident convertToEntity(IncidentDto dto) {
        Incident.IncidentBuilder builder = Incident.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .severity(dto.getSeverity())
                .status(dto.getStatus())
                .source(dto.getSource())
                .priority(dto.getPriority() != null ? dto.getPriority() : "P3");

        if (dto.getAssignedToId() != null) {
            User user = userRepository.findById(dto.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("User not found with id: " + dto.getAssignedToId()));
            builder.assignedTo(user);
        }

        if (dto.getAssetId() != null) {
            backend.entity.Asset asset = assetRepository.findById(dto.getAssetId())
                    .orElseThrow(() -> new RuntimeException("Asset not found with id: " + dto.getAssetId()));
            builder.asset(asset);
        }

        return builder.build();
    }

    @Transactional
    public void resetIncidents() {
        playbookNotificationRepository.deleteAll();
        playbookExecutionLogRepository.deleteAll();
        playbookExecutionRepository.deleteAll();
        auditLogRepository.deleteAll();
        playbookAuditLogRepository.deleteAll();
        incidentRepository.deleteAll();

        seedIncident("Brute Force Detection on Main Portal",
                "Detected 15 failed authentication attempts for user 'admin' within 2 minutes from IP 192.168.1.105.",
                "High", "Open", "Auth Service");

        seedIncident("Malware Suspect on WS-908",
                "Unrecognized hash executed in temporary folder. Target: C:\\Users\\Public\\Temp\\backdoor.exe.",
                "Critical", "Investigating", "EDR Agent");

        seedIncident("Privilege Escalation on Domain Controller",
                "Unusual privilege elevation detected for user 'service_account_temp' on domain controller.",
                "Critical", "Open", "Active Directory");

        seedIncident("Vulnerability Scan Required for Web Server Subnet",
                "Scheduled scan failed to complete due to timeout. Manual intervention or re-triggering required.",
                "Medium", "Open", "Vulnerability Scanner");

        seedIncident("Suspicious PowerShell Execution (Malware Suspect)",
                "Encoded PowerShell command executed by user 'developer_temp' on host WS-102. Action suggests potential malware downloader payload.",
                "High", "Open", "EDR Agent");

        seedIncident("Unauthorized Database Access (Privilege Escalation Suspect)",
                "User account 'analyst_temp' attempted to run high-privilege queries on production database DB-PROD-01, triggering privilege escalation detection rules.",
                "Critical", "Open", "Active Directory");
    }

    private void seedIncident(String title, String description, String severity, String status, String source) {
        Incident incident = Incident.builder()
                .title(title)
                .description(description)
                .severity(severity)
                .status(status)
                .source(source)
                .build();
        setDeadlineBasedOnPriority(incident, "P3");
        incident.setCreatedAt(LocalDateTime.now());
        incident.setUpdatedAt(LocalDateTime.now());
        incidentRepository.save(incident);
    }

    @Transactional
    public IncidentDto linkKbArticle(Long incidentId, Long articleId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + incidentId));
        KnowledgeBaseArticle article = kbArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("KB Article not found with id: " + articleId));

        if (!incident.getKbArticles().contains(article)) {
            incident.getKbArticles().add(article);
            Incident saved = incidentRepository.save(incident);
            
            auditLogService.createLog(
                    "UPDATE",
                    "Linked KB Article: " + article.getTitle(),
                    getCurrentUser(),
                    saved
            );
            return convertToDto(saved);
        }
        return convertToDto(incident);
    }

    @Transactional
    public IncidentDto unlinkKbArticle(Long incidentId, Long articleId) {
        Incident incident = incidentRepository.findById(incidentId)
                .orElseThrow(() -> new RuntimeException("Incident not found with id: " + incidentId));
        KnowledgeBaseArticle article = kbArticleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("KB Article not found with id: " + articleId));

        if (incident.getKbArticles().contains(article)) {
            incident.getKbArticles().remove(article);
            Incident saved = incidentRepository.save(incident);
            
            auditLogService.createLog(
                    "UPDATE",
                    "Unlinked KB Article: " + article.getTitle(),
                    getCurrentUser(),
                    saved
            );
            return convertToDto(saved);
        }
        return convertToDto(incident);
    }
}