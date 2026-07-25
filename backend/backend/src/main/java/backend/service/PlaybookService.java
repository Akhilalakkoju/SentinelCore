package backend.service;

import backend.dto.*;
import backend.entity.*;
import backend.repository.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class PlaybookService {

        private final PlaybookRepository playbookRepository;
        private final PlaybookStepRepository playbookStepRepository;
        private final PlaybookExecutionRepository playbookExecutionRepository;
        private final PlaybookExecutionLogRepository playbookExecutionLogRepository;
        private final PlaybookAuditLogRepository playbookAuditLogRepository;
        private final PlaybookNotificationRepository playbookNotificationRepository;
        private final IncidentRepository incidentRepository;
        private final AuditLogService auditLogService;

        private final ExecutorService executorService = Executors.newCachedThreadPool();

        // ================= Database Seeder =================
        @PostConstruct
        @Transactional
        public void seedDefaultPlaybooks() {
                // Seed sample incidents idempotently
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

                seedIncident("Unauthorized Login Location Detected",
                                "Detected successful user login for 'admin' from anomalous location (Moscow, RU - IP 185.220.101.5), violating geo-fencing policy.",
                                "High", "Open", "GeoIP Security Guard");

                // Seed playbooks & steps idempotently
                seedBruteForcePlaybook();
                seedUnauthorizedLoginLocationPlaybook();
                seedPrivEscPlaybook();
                seedVulnScanPlaybook();

                // Clean up any deprecated Malware Detection Response playbook if still in DB
                playbookRepository.findByName("Malware Detection Response").ifPresent(pb -> {
                        log.info("Cleaning up deprecated Malware Detection Response playbook from DB...");
                        playbookRepository.delete(pb);
                });

                log.info("Playbook and incident seeding completed successfully.");
        }

        private void seedIncident(String title, String description, String severity, String status, String source) {
                if (incidentRepository.findByTitle(title).isEmpty()) {
                        log.info("Seeding incident: {}", title);
                        Incident incident = Incident.builder()
                                        .title(title)
                                        .description(description)
                                        .severity(severity)
                                        .status(status)
                                        .source(source)
                                        .build();
                        incidentRepository.save(incident);
                }
        }

        private void seedBruteForcePlaybook() {
                Playbook bruteForce = playbookRepository.findByName("Brute Force Response").orElse(null);
                if (bruteForce == null) {
                        log.info("Seeding Brute Force Response playbook...");
                        bruteForce = Playbook.builder()
                                        .name("Brute Force Response")
                                        .description("Triggered when multiple failed login attempts are detected. Automatically blocks malicious IP and locks targeted user account.")
                                        .triggerType("ALERT_TYPE")
                                        .triggerValue("Brute Force")
                                        .conditionsJson("{\"failedAttemptsThreshold\":5}")
                                        .estimatedTime("10–15 minutes")
                                        .isActive(true)
                                        .build();

                        bruteForce = playbookRepository.save(bruteForce);

                        PlaybookStep bfStep1 = PlaybookStep.builder()
                                        .playbook(bruteForce)
                                        .stepOrder(1)
                                        .name("Block Attacking Source IP")
                                        .actionType("BLOCK_IP")
                                        .parametersJson("{\"firewallRule\":\"Deny\",\"durationMinutes\":1440}")
                                        .build();

                        PlaybookStep bfStep2 = PlaybookStep.builder()
                                        .playbook(bruteForce)
                                        .stepOrder(2)
                                        .name("Temporarily Lock Compromised Account")
                                        .actionType("DISABLE_USER")
                                        .parametersJson("{\"lockDurationMinutes\":60}")
                                        .build();

                        PlaybookStep bfStep3 = PlaybookStep.builder()
                                        .playbook(bruteForce)
                                        .stepOrder(3)
                                        .name("Send SOC Alert Notification")
                                        .actionType("SEND_NOTIFICATION")
                                        .parametersJson("{\"channel\":\"SLACK\",\"recipient\":\"#soc-alerts\",\"severity\":\"Critical\"}")
                                        .build();

                        PlaybookStep bfStep4 = PlaybookStep.builder()
                                        .playbook(bruteForce)
                                        .stepOrder(4)
                                        .name("Create Incident Ticket")
                                        .actionType("CREATE_INCIDENT")
                                        .parametersJson("{\"title\":\"Brute Force Attack Contained\",\"severity\":\"High\"}")
                                        .build();

                        playbookStepRepository.saveAll(List.of(bfStep1, bfStep2, bfStep3, bfStep4));
                } else if (bruteForce.getEstimatedTime() == null) {
                        bruteForce.setEstimatedTime("10–15 minutes");
                        playbookRepository.save(bruteForce);
                }
        }

        private void seedUnauthorizedLoginLocationPlaybook() {
                Playbook locationPb = playbookRepository.findByName("Unauthorized Login Location Detection").orElse(null);
                
                if (locationPb == null) {
                        // Fall back to legacy Malware Detection & Containment playbooks to rename in place and preserve execution foreign keys
                        locationPb = playbookRepository.findByName("Malware Detection & Containment")
                                        .or(() -> playbookRepository.findByName("Malware Containment"))
                                        .or(() -> playbookRepository.findByName("Malware Detection Response"))
                                        .orElse(null);
                        if (locationPb != null) {
                                log.info("Updating legacy Malware playbook to Unauthorized Login Location Detection in place...");
                                locationPb.setName("Unauthorized Login Location Detection");
                                locationPb.setDescription("Standard procedure for responding to unauthorized login location and impossible travel incidents.");
                                locationPb.setTriggerType("ALERT_TYPE");
                                locationPb.setTriggerValue("Unauthorized Login Location");
                                locationPb.setEstimatedTime("20–30 minutes");
                                
                                locationPb.getSteps().clear();
                                locationPb = playbookRepository.saveAndFlush(locationPb);
                        }
                }

                if (locationPb == null) {
                        log.info("Seeding Unauthorized Login Location Detection playbook...");
                        locationPb = Playbook.builder()
                                        .name("Unauthorized Login Location Detection")
                                        .description("Standard procedure for responding to unauthorized login location and impossible travel incidents.")
                                        .triggerType("ALERT_TYPE")
                                        .triggerValue("Unauthorized Login Location")
                                        .estimatedTime("20–30 minutes")
                                        .isActive(true)
                                        .build();

                        locationPb = playbookRepository.save(locationPb);
                }

                if (locationPb.getSteps() == null || locationPb.getSteps().isEmpty()) {
                        PlaybookStep locStep1 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(1)
                                        .name("Verify login alert and IP location details")
                                        .actionType("MANUAL")
                                        .build();

                        PlaybookStep locStep2 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(2)
                                        .name("Block suspicious IP address / Geo-location")
                                        .actionType("BLOCK_IP")
                                        .parametersJson("{\"firewallRule\":\"Deny\",\"durationMinutes\":1440}")
                                        .build();

                        PlaybookStep locStep3 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(3)
                                        .name("Disable compromised user account & revoke active sessions")
                                        .actionType("DISABLE_USER")
                                        .parametersJson("{\"revokeTokens\":true}")
                                        .build();

                        PlaybookStep locStep4 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(4)
                                        .name("Notify SOC response team")
                                        .actionType("SEND_NOTIFICATION")
                                        .parametersJson("{\"channel\":\"SLACK\",\"recipient\":\"#soc-alerts\"}")
                                        .build();

                        PlaybookStep locStep5 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(5)
                                        .name("Collect authentication logs & IP telemetry")
                                        .actionType("MANUAL")
                                        .build();

                        PlaybookStep locStep6 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(6)
                                        .name("Force password reset & multi-factor auth re-enrollment")
                                        .actionType("MANUAL")
                                        .build();

                        PlaybookStep locStep7 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(7)
                                        .name("Restore account access with restricted security policy")
                                        .actionType("MANUAL")
                                        .build();

                        PlaybookStep locStep8 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(8)
                                        .name("Verify account & login activity health")
                                        .actionType("MANUAL")
                                        .build();

                        PlaybookStep locStep9 = PlaybookStep.builder()
                                        .playbook(locationPb)
                                        .stepOrder(9)
                                        .name("Update incident ticket")
                                        .actionType("MANUAL")
                                        .build();

                        playbookStepRepository.saveAll(List.of(locStep1, locStep2, locStep3, locStep4, locStep5, locStep6, locStep7, locStep8, locStep9));
                }
        }

        private void seedPrivEscPlaybook() {
                Playbook privEsc = playbookRepository.findByName("Privilege Escalation Detection").orElse(null);
                if (privEsc == null) {
                        log.info("Seeding Privilege Escalation Detection playbook...");
                        privEsc = Playbook.builder()
                                        .name("Privilege Escalation Detection")
                                        .description("Fires when an unauthorized permission elevation is caught. Deactivates credentials immediately.")
                                        .triggerType("THREAT_DETECTED")
                                        .triggerValue("Privilege Escalation")
                                        .estimatedTime("15–20 minutes")
                                        .isActive(true)
                                        .build();

                        privEsc = playbookRepository.save(privEsc);

                        PlaybookStep peStep1 = PlaybookStep.builder()
                                        .playbook(privEsc)
                                        .stepOrder(1)
                                        .name("Disable Account & Revoke Roles")
                                        .actionType("DISABLE_USER")
                                        .parametersJson("{\"immediateDeactivation\":true}")
                                        .build();

                        PlaybookStep peStep2 = PlaybookStep.builder()
                                        .playbook(privEsc)
                                        .stepOrder(2)
                                        .name("Trigger High Priority Notification")
                                        .actionType("SEND_NOTIFICATION")
                                        .parametersJson("{\"channel\":\"IN_APP\",\"recipient\":\"ADMIN\"}")
                                        .build();

                        PlaybookStep peStep3 = PlaybookStep.builder()
                                        .playbook(privEsc)
                                        .stepOrder(3)
                                        .name("File Privilege Abuse Incident")
                                        .actionType("CREATE_INCIDENT")
                                        .parametersJson("{\"title\":\"Unauthorized Privilege Elevation Detected\",\"severity\":\"Critical\"}")
                                        .build();

                        playbookStepRepository.saveAll(List.of(peStep1, peStep2, peStep3));
                } else if (privEsc.getEstimatedTime() == null) {
                        privEsc.setEstimatedTime("15–20 minutes");
                        playbookRepository.save(privEsc);
                }
        }

        private void seedVulnScanPlaybook() {
                Playbook vulnScan = playbookRepository.findByName("Vulnerability Scan Automation").orElse(null);
                if (vulnScan == null) {
                        log.info("Seeding Vulnerability Scan Automation playbook...");
                        vulnScan = Playbook.builder()
                                        .name("Vulnerability Scan Automation")
                                        .description("Triggers a dynamic vulnerability scan on the network subnet and generates remediation tickets.")
                                        .triggerType("MANUAL")
                                        .estimatedTime("60–90 minutes")
                                        .isActive(true)
                                        .build();

                        vulnScan = playbookRepository.save(vulnScan);

                        PlaybookStep vsStep1 = PlaybookStep.builder()
                                        .playbook(vulnScan)
                                        .stepOrder(1)
                                        .name("Trigger Subnet Vulnerability Scan")
                                        .actionType("SCAN_VULNERABILITY")
                                        .parametersJson("{\"targetSubnet\":\"10.0.1.0/24\",\"scanProfile\":\"Full Discovery\"}")
                                        .build();

                        PlaybookStep vsStep2 = PlaybookStep.builder()
                                        .playbook(vulnScan)
                                        .stepOrder(2)
                                        .name("Send Scan Summary Report")
                                        .actionType("SEND_NOTIFICATION")
                                        .parametersJson("{\"channel\":\"EMAIL\",\"recipient\":\"admin@sentinelcore.com\"}")
                                        .build();

                        PlaybookStep vsStep3 = PlaybookStep.builder()
                                        .playbook(vulnScan)
                                        .stepOrder(3)
                                        .name("Create Remediation Tickets")
                                        .actionType("CREATE_INCIDENT")
                                        .parametersJson("{\"title\":\"Vulnerability Scan Remediation Tasks\",\"severity\":\"Medium\"}")
                                        .build();

                        playbookStepRepository.saveAll(List.of(vsStep1, vsStep2, vsStep3));
                } else if (vulnScan.getEstimatedTime() == null) {
                        vulnScan.setEstimatedTime("60–90 minutes");
                        playbookRepository.save(vulnScan);
                }
        }

        // ================= Config CRUD Operations =================
        @Transactional(readOnly = true)
        public List<PlaybookDto> getAllPlaybooks() {
                return playbookRepository.findAll().stream()
                                .map(this::convertToDto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public PlaybookDto getPlaybookById(Long id) {
                Playbook playbook = playbookRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Playbook not found with id: " + id));
                return convertToDto(playbook);
        }

        @Transactional
        public PlaybookDto createPlaybook(PlaybookDto dto) {
                Playbook playbook = Playbook.builder()
                                .name(dto.getName())
                                .description(dto.getDescription())
                                .triggerType(dto.getTriggerType())
                                .triggerValue(dto.getTriggerValue())
                                .conditionsJson(dto.getConditionsJson())
                                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                                .estimatedTime(dto.getEstimatedTime())
                                .build();

                Playbook savedPlaybook = playbookRepository.save(playbook);

                if (dto.getSteps() != null) {
                        List<PlaybookStep> steps = dto.getSteps().stream()
                                        .map(stepDto -> PlaybookStep.builder()
                                                        .playbook(savedPlaybook)
                                                        .stepOrder(stepDto.getStepOrder())
                                                        .name(stepDto.getName())
                                                        .actionType(stepDto.getActionType())
                                                        .parametersJson(stepDto.getParametersJson())
                                                        .build())
                                        .collect(Collectors.toList());
                        playbookStepRepository.saveAll(steps);
                        savedPlaybook.setSteps(steps);
                }

                // Audit Log
                saveAuditLog("CREATE_PLAYBOOK", savedPlaybook.getId(),
                                "Created playbook configuration: " + savedPlaybook.getName());

                return convertToDto(savedPlaybook);
        }

        @Transactional
        public PlaybookDto updatePlaybook(Long id, PlaybookDto dto) {
                Playbook playbook = playbookRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Playbook not found with id: " + id));

                playbook.setName(dto.getName());
                playbook.setDescription(dto.getDescription());
                playbook.setTriggerType(dto.getTriggerType());
                playbook.setTriggerValue(dto.getTriggerValue());
                playbook.setConditionsJson(dto.getConditionsJson());
                playbook.setEstimatedTime(dto.getEstimatedTime());
                if (dto.getIsActive() != null) {
                        playbook.setIsActive(dto.getIsActive());
                }

                // Recreate steps
                playbookStepRepository.deleteAll(playbook.getSteps());
                playbook.getSteps().clear();

                if (dto.getSteps() != null) {
                        List<PlaybookStep> steps = dto.getSteps().stream()
                                        .map(stepDto -> PlaybookStep.builder()
                                                        .playbook(playbook)
                                                        .stepOrder(stepDto.getStepOrder())
                                                        .name(stepDto.getName())
                                                        .actionType(stepDto.getActionType())
                                                        .parametersJson(stepDto.getParametersJson())
                                                        .build())
                                        .collect(Collectors.toList());
                        playbookStepRepository.saveAll(steps);
                        playbook.setSteps(steps);
                }

                Playbook updated = playbookRepository.save(playbook);

                // Audit Log
                saveAuditLog("UPDATE_PLAYBOOK", updated.getId(),
                                "Updated playbook configuration: " + updated.getName());

                return convertToDto(updated);
        }

        @Transactional
        public PlaybookDto togglePlaybookStatus(Long id) {
                Playbook playbook = playbookRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Playbook not found with id: " + id));
                playbook.setIsActive(!playbook.getIsActive());
                Playbook saved = playbookRepository.save(playbook);

                // Audit Log
                saveAuditLog("UPDATE_PLAYBOOK", saved.getId(),
                                "Toggled active status of playbook: " + saved.getName() + " to " + saved.getIsActive());

                return convertToDto(saved);
        }

        @Transactional
        public void deletePlaybook(Long id) {
                Playbook playbook = playbookRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException("Playbook not found with id: " + id));
                playbookRepository.delete(playbook);

                // Audit Log
                saveAuditLog("DELETE_PLAYBOOK", id, "Deleted playbook configuration: " + playbook.getName());
        }

        // ================= Playbook Execution Engine =================

        public PlaybookExecutionDto triggerPlaybook(Long playbookId, Long incidentId, User triggeredBy) {
                return triggerPlaybook(playbookId, incidentId, triggeredBy, false);
        }

        public PlaybookExecutionDto triggerPlaybook(Long playbookId, Long incidentId, User triggeredBy, boolean runAsync) {
                Playbook playbook = playbookRepository.findById(playbookId)
                                .orElseThrow(() -> new RuntimeException("Playbook not found with id: " + playbookId));

                if (!playbook.getIsActive()) {
                        throw new RuntimeException("Playbook is inactive");
                }

                Incident incident = null;
                if (incidentId != null) {
                        incident = incidentRepository.findById(incidentId).orElse(null);
                        if (incident != null) {
                                if ("Resolved".equalsIgnoreCase(incident.getStatus()) || "Closed".equalsIgnoreCase(incident.getStatus())) {
                                        throw new RuntimeException("Cannot run playbook on a resolved or closed incident");
                                }
                                if (!isPlaybookRelevant(playbook, incident)) {
                                        throw new RuntimeException("This playbook is not relevant to this incident. Please select the relevant playbook.");
                                }
                        }
                }

                PlaybookExecution execution = PlaybookExecution.builder()
                                .playbook(playbook)
                                .playbookName(playbook.getName())
                                .incident(incident)
                                .incidentId(incidentId)
                                .status("PENDING")
                                .currentStep("Queued for execution")
                                .currentStepIndex(0)
                                .progress(0)
                                .triggeredBy(triggeredBy)
                                .startedAt(LocalDateTime.now())
                                .build();

                execution = playbookExecutionRepository.save(execution);

                if (runAsync) {
                        runAsyncExecution(execution.getId());
                } else {
                        writeExecutionLog(execution, "System", "PENDING", "INFO",
                                        "Playbook execution queued and ready for interactive response.");
                }

                // Audit Log
                saveAuditLog("TRIGGER_PLAYBOOK", execution.getId(),
                                "Triggered playbook: " + playbook.getName() + " on incident ID: " + incidentId);

                return convertToExecutionDto(execution);
        }

        @Transactional
        public PlaybookExecutionDto startExecution(Long executionId, User user) {
                PlaybookExecution execution = playbookExecutionRepository.findById(executionId)
                                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

                if (!"PENDING".equalsIgnoreCase(execution.getStatus())) {
                        throw new RuntimeException("Execution already started");
                }

                execution.setStatus("RUNNING");
                execution.setStartedAt(LocalDateTime.now());
                execution.setTriggeredBy(user);
                execution = playbookExecutionRepository.save(execution);

                // Log in execution log
                writeExecutionLog(execution, "System", "RUNNING", "INFO", "Playbook response sequence initiated by " + (user != null ? user.getName() : "System") + ".");

                // If incident is associated, transition status to Investigating
                if (execution.getIncidentId() != null) {
                        Incident incident = incidentRepository.findById(execution.getIncidentId()).orElse(null);
                        if (incident != null) {
                                String oldStatus = incident.getStatus();
                                incident.setStatus("Investigating");
                                incidentRepository.save(incident);

                                // Write incident audit log
                                auditLogService.createLog("PLAYBOOK_STARTED", 
                                                "Playbook " + execution.getPlaybookName() + " started. Incident status updated from " + oldStatus + " to Investigating.",
                                                user, incident);
                        }
                }

                saveAuditLog("START_PLAYBOOK", executionId, "Started playbook execution: " + execution.getPlaybookName());

                return convertToExecutionDto(execution);
        }

        @Transactional
        public PlaybookExecutionDto executeStep(Long executionId, Integer stepOrder, User user) {
                PlaybookExecution execution = playbookExecutionRepository.findById(executionId)
                                .orElseThrow(() -> new RuntimeException("Execution not found: " + executionId));

                if (!"RUNNING".equalsIgnoreCase(execution.getStatus())) {
                        throw new RuntimeException("Playbook execution is not in RUNNING state.");
                }

                List<PlaybookStep> steps = execution.getPlaybook().getSteps();
                if (stepOrder < 1 || stepOrder > steps.size()) {
                        throw new RuntimeException("Invalid step sequence: " + stepOrder);
                }

                PlaybookStep step = steps.get(stepOrder - 1);
                execution.setCurrentStep(step.getName());
                execution.setCurrentStepIndex(stepOrder);
                
                int progress = (int) (((double) stepOrder / steps.size()) * 100);
                execution.setProgress(progress);
                playbookExecutionRepository.save(execution);

                // Write execution log
                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO", "Executing Action: " + step.getActionType());

                Incident incident = null;
                if (execution.getIncidentId() != null) {
                        incident = incidentRepository.findById(execution.getIncidentId()).orElse(null);
                }

                // Execute action
                String actionStr = step.getActionType();
                try {
                        performAction(execution, step);
                        writeExecutionLog(execution, step.getName(), "SUCCESS", "INFO", "Action successfully finished.");
                        
                        // Map step to incident audit logs & status transitions
                        if (incident != null) {
                                String details = "Playbook step '" + step.getName() + "' executed successfully.";
                                String actionKey = "PLAYBOOK_STEP";
                                
                                if ("ISOLATE_HOST".equalsIgnoreCase(actionStr)) {
                                        actionKey = "HOST_ISOLATED";
                                        details = "Endpoint host isolated from internal network connection.";
                                        
                                        String oldStatus = incident.getStatus();
                                        incident.setStatus("Contained");
                                        incidentRepository.save(incident);
                                        writeExecutionLog(execution, "System", "RUNNING", "INFO", "Associated incident status transitioned from " + oldStatus + " to 'Contained'.");
                                } else if ("DISABLE_USER".equalsIgnoreCase(actionStr)) {
                                        actionKey = "USER_DISABLED";
                                        details = "Compromised user credentials and active directory token revoked.";
                                        
                                        String oldStatus = incident.getStatus();
                                        incident.setStatus("Contained");
                                        incidentRepository.save(incident);
                                        writeExecutionLog(execution, "System", "RUNNING", "INFO", "Associated incident status transitioned from " + oldStatus + " to 'Contained'.");
                                } else if ("BLOCK_IP".equalsIgnoreCase(actionStr)) {
                                        actionKey = "IP_BLOCKED";
                                        details = "Attacking source IP block injected to PaloAlto Firewall rule.";
                                        
                                        String oldStatus = incident.getStatus();
                                        incident.setStatus("Contained");
                                        incidentRepository.save(incident);
                                        writeExecutionLog(execution, "System", "RUNNING", "INFO", "Associated incident status transitioned from " + oldStatus + " to 'Contained'.");
                                } else if ("SEND_NOTIFICATION".equalsIgnoreCase(actionStr)) {
                                        actionKey = "NOTIFICATION_SENT";
                                        details = "Incident response alert notification sent to SOC Channel.";
                                } else if ("SCAN_VULNERABILITY".equalsIgnoreCase(actionStr)) {
                                        actionKey = "VULNERABILITY_SCAN";
                                        details = "Quarantined subnet scanned for local vulnerabilities.";
                                } else {
                                        // Custom mapping based on name
                                        String nameLower = step.getName().toLowerCase();
                                        if (nameLower.contains("remove") || nameLower.contains("malware") || nameLower.contains("clean")) {
                                                actionKey = "MALWARE_REMOVED";
                                                details = "Malware containment cleanup executed. Malicious file hashes quarantined.";
                                                
                                                String oldStatus = incident.getStatus();
                                                incident.setStatus("Recovery");
                                                incidentRepository.save(incident);
                                                writeExecutionLog(execution, "System", "RUNNING", "INFO", "Associated incident status transitioned from " + oldStatus + " to 'Recovery'.");
                                        } else if (nameLower.contains("restore") || nameLower.contains("recover")) {
                                                actionKey = "SYSTEM_RESTORED";
                                                details = "System restored from clean backups.";
                                                
                                                String oldStatus = incident.getStatus();
                                                incident.setStatus("Recovery");
                                                incidentRepository.save(incident);
                                                writeExecutionLog(execution, "System", "RUNNING", "INFO", "Associated incident status transitioned from " + oldStatus + " to 'Recovery'.");
                                        } else if (nameLower.contains("verify") || nameLower.contains("health")) {
                                                actionKey = "SYSTEM_VERIFIED";
                                                details = "System verified health status is clean and normal.";
                                        } else if (nameLower.contains("soc") || nameLower.contains("alert")) {
                                                actionKey = "SOC_NOTIFIED";
                                                details = "Incident response alert notification sent to SOC Channel.";
                                        } else if (nameLower.contains("incident") || nameLower.contains("ticket")) {
                                                actionKey = "INCIDENT_UPDATED";
                                                details = "Associated incident ticket log audit update.";
                                        }
                                }

                                auditLogService.createLog(actionKey, details, user, incident);
                        }
                } catch (Exception e) {
                        writeExecutionLog(execution, step.getName(), "FAILED", "ERROR", "Action failed: " + e.getMessage());
                        execution.setStatus("FAILED");
                        execution.setEndedAt(LocalDateTime.now());
                        playbookExecutionRepository.save(execution);
                        
                        if (incident != null) {
                                auditLogService.createLog("PLAYBOOK_FAILED", "Playbook step '" + step.getName() + "' failed: " + e.getMessage(), user, incident);
                        }
                        
                        throw new RuntimeException("Failed to execute step action: " + e.getMessage());
                }

                // If this is the last step, mark execution SUCCESS
                if (stepOrder == steps.size()) {
                        execution.setStatus("SUCCESS");
                        execution.setCurrentStep("Execution Completed");
                        execution.setEndedAt(LocalDateTime.now());
                        playbookExecutionRepository.save(execution);

                        writeExecutionLog(execution, "System", "SUCCESS", "INFO", "Playbook finished execution with status SUCCESS.");

                        if (incident != null) {
                                auditLogService.createLog("PLAYBOOK_COMPLETED", "Playbook " + execution.getPlaybookName() + " completed execution successfully.", user, incident);
                        }
                }

                saveAuditLog("STEP_EXECUTE", execution.getId(), "Executed step " + stepOrder + " (" + step.getName() + ") of playbook " + execution.getPlaybookName());

                return convertToExecutionDto(execution);
        }

        private void runAsyncExecution(Long executionId) {
                CompletableFuture.runAsync(() -> {
                        try {
                                // Fetch execution in this thread context
                                PlaybookExecution execution = playbookExecutionRepository.findById(executionId)
                                .orElseThrow(() -> new RuntimeException(
                                                                "Execution not found: " + executionId));

                                List<PlaybookStep> steps = execution.getPlaybook().getSteps();

                                Incident targetIncident = null;
                                if (execution.getIncidentId() != null) {
                                        targetIncident = incidentRepository.findById(execution.getIncidentId()).orElse(null);
                                }
                                boolean isSecondary = false;
                                if (targetIncident != null) {
                                        String relation = getPlaybookRelation(execution.getPlaybook(), targetIncident);
                                        isSecondary = "SECONDARY".equalsIgnoreCase(relation);
                                }

                                // If playbook has no steps, finish immediately
                                if (steps.isEmpty()) {
                                        execution.setStatus("SUCCESS");
                                        execution.setCurrentStep("No steps defined");
                                        execution.setProgress(isSecondary ? 70 : 100);
                                        execution.setEndedAt(LocalDateTime.now());
                                        playbookExecutionRepository.save(execution);
                                        writeExecutionLog(execution, "System", "SUCCESS", "INFO",
                                                        "Playbook execution completed. No steps to run.");
                                        return;
                                }

                                execution.setStatus("RUNNING");
                                playbookExecutionRepository.save(execution);

                                // Transition incident status to Investigating
                                if (targetIncident != null && "Open".equals(targetIncident.getStatus())) {
                                        targetIncident.setStatus("Investigating");
                                        incidentRepository.save(targetIncident);
                                        writeExecutionLog(execution, "System", "RUNNING", "INFO", 
                                                        "Associated incident status transitioned to 'Investigating'.");
                                }

                                writeExecutionLog(execution, "System", "RUNNING", "INFO",
                                                "Starting playbook execution sequence...");

                                int maxProgress = isSecondary ? 70 : 100;
                                for (int i = 0; i < steps.size(); i++) {
                                        PlaybookStep step = steps.get(i);
                                        execution.setCurrentStep(step.getName());
                                        execution.setCurrentStepIndex(i + 1);
                                        int currentProgress = (int) (((double) (i + 1) / steps.size()) * maxProgress);
                                        execution.setProgress(currentProgress);
                                        playbookExecutionRepository.save(execution);

                                        writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                        "Executing Action: " + step.getActionType());

                                        // Simulate execution delay (e.g. 2 seconds)
                                        Thread.sleep(2000);

                                        // Perform action logic
                                        performAction(execution, step);

                                        writeExecutionLog(execution, step.getName(), "SUCCESS", "INFO",
                                                        "Action successfully finished.");
                                }

                                execution.setStatus("SUCCESS");
                                execution.setProgress(isSecondary ? 70 : 100);
                                execution.setCurrentStep("Execution Completed");
                                execution.setEndedAt(LocalDateTime.now());
                                playbookExecutionRepository.save(execution);

                                // Transition incident status to Resolved or Investigating (Partially Resolved) on completion
                                if (targetIncident != null) {
                                        if (isSecondary) {
                                                targetIncident.setStatus("Investigating");
                                                targetIncident.setDescription(targetIncident.getDescription() +
                                                                "\n\n[Playbook Automation] This incident has been partially resolved (70%) by successful execution of secondary playbook: " +
                                                                execution.getPlaybookName() + " (Execution #" + execution.getId() + ").");
                                                incidentRepository.save(targetIncident);
                                                writeExecutionLog(execution, "System", "SUCCESS", "INFO",
                                                                "Associated incident status updated to '''Investigating''' (Partially Resolved) automatically.");
                                        } else {
                                                targetIncident.setStatus("Resolved");
                                                targetIncident.setDescription(targetIncident.getDescription() +
                                                                "\n\n[Playbook Automation] This incident has been automatically resolved by successful execution of playbook: " +
                                                                execution.getPlaybookName() + " (Execution #" + execution.getId() + ").");
                                                incidentRepository.save(targetIncident);
                                                writeExecutionLog(execution, "System", "SUCCESS", "INFO",
                                                                "Associated incident status updated to '''Resolved''' automatically.");
                                        }
                                }


                                writeExecutionLog(execution, "System", "SUCCESS", "INFO",
                                                "Playbook finished execution with status SUCCESS.");

                        } catch (Exception e) {
                                log.error("Failed executing playbook steps asynchronously", e);
                                PlaybookExecution execution = playbookExecutionRepository.findById(executionId)
                                                .orElse(null);
                                if (execution != null) {
                                        execution.setStatus("FAILED");
                                        execution.setCurrentStep("Failed: " + e.getMessage());
                                        execution.setEndedAt(LocalDateTime.now());
                                        playbookExecutionRepository.save(execution);
                                        writeExecutionLog(execution, "System", "FAILED", "ERROR",
                                                        "Playbook run encountered critical error: " + e.getMessage());
                                }
                        }
                }, executorService);
        }

        private void performAction(PlaybookExecution execution, PlaybookStep step) throws Exception {
                String action = step.getActionType();
                String params = step.getParametersJson();

                switch (action) {
                        case "BLOCK_IP":
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Sending command block to PaloAlto Firewall API...");
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Successfully added firewall entry: Drop traffic from source IP indefinitely.");
                                break;
                        case "ISOLATE_HOST":
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Contacting Endpoint Agent (EDR)...");
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Isolating network interface connection. Only communication allowed is to SentinelCore Agent.");
                                break;
                        case "DISABLE_USER":
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Interfacing Active Directory / LDAP Service...");
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Deactivated user account. Access token keys invalidated.");
                                break;
                        case "SCAN_VULNERABILITY":
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Spinning up target vulnerability profile scan...");
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Vulnerability scan completed. 0 High, 2 Medium vulnerabilities discovered.");
                                break;
                        case "SEND_NOTIFICATION":
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Compiling alert email/Slack template...");
                                PlaybookNotification notif = PlaybookNotification.builder()
                                                .playbookExecution(execution)
                                                .recipient("soc-channel@sentinelcore.com")
                                                .channel("EMAIL")
                                                .message("Automated Action: " + execution.getPlaybookName()
                                                                + " has run step [" + step.getName()
                                                                + "] for Incident #" + execution.getIncidentId())
                                                .status("SENT")
                                                .build();
                                playbookNotificationRepository.save(notif);
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO", "Alert sent to: "
                                                + notif.getRecipient() + " over " + notif.getChannel());
                                break;
                        case "CREATE_INCIDENT":
                                // If triggered on an existing incident, update it instead of creating a duplicate
                                if (execution.getIncidentId() != null) {
                                        Incident existing = incidentRepository.findById(execution.getIncidentId()).orElse(null);
                                        if (existing != null) {
                                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                                "Incident #" + existing.getId() + " already exists. Appending action logs to current ticket.");
                                                existing.setDescription(existing.getDescription() + 
                                                                "\n\n[Playbook Step: " + step.getName() + "] Action parameters: " + params);
                                                incidentRepository.save(existing);
                                                break;
                                        }
                                }

                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Generating ticket details...");
                                Incident incident = Incident.builder()
                                                .title("Auto: " + execution.getPlaybookName() + " Triggered Action")
                                                .description("Automated response incident opened during execution of playbook: "
                                                                + execution.getPlaybookName() + ". Action parameters: "
                                                                + params)
                                                .severity("High")
                                                .status("Open")
                                                .source("Playbook Engine")
                                                .build();
                                incident = incidentRepository.save(incident);

                                // Link this execution to the newly created incident if it was null
                                if (execution.getIncident() == null) {
                                        execution.setIncident(incident);
                                        execution.setIncidentId(incident.getId());
                                }
                                writeExecutionLog(execution, step.getName(), "RUNNING", "INFO",
                                                "Created new Incident ticket record. Ticket ID: #" + incident.getId());
                                break;
                        default:
                                throw new IllegalArgumentException("Unknown playbook step action: " + action);
                }
        }

        private void writeExecutionLog(PlaybookExecution execution, String stepName, String status, String level,
                        String message) {
                PlaybookExecutionLog executionLog = PlaybookExecutionLog.builder()
                                .playbookExecution(execution)
                                .stepName(stepName)
                                .status(status)
                                .logLevel(level)
                                .message(message)
                                .build();
                playbookExecutionLogRepository.save(executionLog);
        }

        private void saveAuditLog(String actionType, Long entityId, String details) {
                PlaybookAuditLog audit = PlaybookAuditLog.builder()
                                .actionType(actionType)
                                .entityId(entityId)
                                .details(details)
                                .build();
                playbookAuditLogRepository.save(audit);
        }

        // ================= History Queries =================
        @Transactional(readOnly = true)
        public List<PlaybookExecutionDto> getExecutionHistory() {
                return playbookExecutionRepository.findAllByOrderByIdDesc().stream()
                                .map(this::convertToExecutionDto)
                                .collect(Collectors.toList());
        }

        @Transactional(readOnly = true)
        public PlaybookExecutionDto getExecutionDetails(Long executionId) {
                PlaybookExecution exec = playbookExecutionRepository.findById(executionId)
                                .orElseThrow(() -> new RuntimeException(
                                                "Execution details not found with id: " + executionId));
                return convertToExecutionDto(exec);
        }

        @Transactional(readOnly = true)
        public List<PlaybookExecutionLogDto> getExecutionLogs(Long executionId) {
                return playbookExecutionLogRepository.findByPlaybookExecutionIdOrderByTimestampAsc(executionId).stream()
                                .map(log -> PlaybookExecutionLogDto.builder()
                                                .id(log.getId())
                                                .executionId(executionId)
                                                .stepName(log.getStepName())
                                                .status(log.getStatus())
                                                .logLevel(log.getLogLevel())
                                                .message(log.getMessage())
                                                .timestamp(log.getTimestamp())
                                                .build())
                                .collect(Collectors.toList());
        }

        // ================= Mappers =================
        private PlaybookDto convertToDto(Playbook playbook) {
                List<PlaybookStepDto> steps = playbook.getSteps().stream()
                                .map(step -> PlaybookStepDto.builder()
                                                .id(step.getId())
                                                .stepOrder(step.getStepOrder())
                                                .name(step.getName())
                                                .actionType(step.getActionType())
                                                .parametersJson(step.getParametersJson())
                                                .build())
                                .collect(Collectors.toList());

                return PlaybookDto.builder()
                                .id(playbook.getId())
                                .name(playbook.getName())
                                .description(playbook.getDescription())
                                .triggerType(playbook.getTriggerType())
                                .triggerValue(playbook.getTriggerValue())
                                .conditionsJson(playbook.getConditionsJson())
                                .isActive(playbook.getIsActive())
                                .estimatedTime(playbook.getEstimatedTime())
                                .steps(steps)
                                .createdAt(playbook.getCreatedAt())
                                .updatedAt(playbook.getUpdatedAt())
                                .build();
        }

        private PlaybookExecutionDto convertToExecutionDto(PlaybookExecution exec) {
                return PlaybookExecutionDto.builder()
                                .id(exec.getId())
                                .playbookId(exec.getPlaybook() != null ? exec.getPlaybook().getId() : null)
                                .playbookName(exec.getPlaybookName())
                                .incidentId(exec.getIncidentId())
                                .incidentTitle(exec.getIncident() != null ? exec.getIncident().getTitle() : null)
                                .status(exec.getStatus())
                                .currentStep(exec.getCurrentStep())
                                .currentStepIndex(exec.getCurrentStepIndex())
                                .progress(exec.getProgress())
                                .triggeredById(exec.getTriggeredBy() != null ? exec.getTriggeredBy().getId() : null)
                                .triggeredByName(exec.getTriggeredBy() != null ? exec.getTriggeredBy().getName() : null)
                                .startedAt(exec.getStartedAt())
                                .endedAt(exec.getEndedAt())
                                .createdAt(exec.getCreatedAt())
                                .build();
        }

        // ================= Compatibility Wrapper =================
        // Retains compatibility with the original frontend/controller runPlaybook and
        // updateStatus methods.
        @Transactional
        public PlaybookStatusResponse runPlaybook(Long incidentId) {
                // Run default Unauthorized Login Location Detection playbook
                Playbook locationPb = playbookRepository.findByName("Unauthorized Login Location Detection")
                                .or(() -> playbookRepository.findAll().stream().filter(Playbook::getIsActive).findFirst())
                                .orElseThrow(() -> new RuntimeException(
                                                "Default response playbook not seeded"));

                PlaybookExecutionDto dto = triggerPlaybook(locationPb.getId(), incidentId, null, true);
                return new PlaybookStatusResponse(
                                dto.getIncidentId(),
                                dto.getStatus(),
                                dto.getCurrentStep(),
                                dto.getProgress());
        }

        @Transactional
        public PlaybookStatusResponse updateStatus(Long incidentId) {
                PlaybookExecution exec = playbookExecutionRepository.findByIncidentId(incidentId)
                                .orElseThrow(() -> new RuntimeException(
                                                "No execution found for incident: " + incidentId));

                return new PlaybookStatusResponse(
                                exec.getIncidentId(),
                                exec.getStatus(),
                                exec.getCurrentStep(),
                                exec.getProgress());
        }

        private int getEditDistance(String a, String b) {
                if (a.length() == 0) return b.length();
                if (b.length() == 0) return a.length();
                int[][] matrix = new int[b.length() + 1][a.length() + 1];
                for (int i = 0; i <= b.length(); i++) matrix[i][0] = i;
                for (int j = 0; j <= a.length(); j++) matrix[0][j] = j;
                for (int i = 1; i <= b.length(); i++) {
                        for (int j = 1; j <= a.length(); j++) {
                                if (b.charAt(i - 1) == a.charAt(j - 1)) {
                                        matrix[i][j] = matrix[i - 1][j - 1];
                                } else {
                                        matrix[i][j] = Math.min(
                                                matrix[i - 1][j - 1] + 1,
                                                Math.min(matrix[i][j - 1] + 1, matrix[i - 1][j] + 1)
                                        );
                                }
                        }
                }
                return matrix[b.length()][a.length()];
        }

        private boolean wordsMatchFuzzy(String w1, String w2) {
                if (w1.equals(w2)) return true;
                if (w1.startsWith(w2) || w2.startsWith(w1)) return true;
                int len1 = w1.length();
                int len2 = w2.length();
                int dist = getEditDistance(w1, w2);
                if (len1 >= 7 && len2 >= 7 && dist <= 2) return true;
                if (len1 >= 5 && len2 >= 5 && dist <= 1) return true;
                return false;
        }

        private java.util.List<String> getCleanWords(String text, java.util.Set<String> stopWords) {
                if (text == null || text.isBlank()) {
                        return java.util.List.of();
                }
                return java.util.Arrays.stream(text.toLowerCase().split("[\\s_\\-]+"))
                        .map(word -> word.replaceAll("[^a-z0-9]", ""))
                        .filter(word -> word.length() > 2 && !stopWords.contains(word))
                        .collect(Collectors.toList());
        }

        private boolean isPlaybookRelevant(Playbook playbook, Incident incident) {
                if (playbook == null || incident == null) {
                        return false;
                }

                // 1. MANUAL trigger type is always relevant
                if ("MANUAL".equalsIgnoreCase(playbook.getTriggerType())) {
                        return true;
                }

                // 2. Severity match
                if ("ALERT_SEVERITY".equalsIgnoreCase(playbook.getTriggerType()) &&
                                playbook.getTriggerValue() != null &&
                                playbook.getTriggerValue().equalsIgnoreCase(incident.getSeverity())) {
                        return true;
                }

                // 3. Trigger value substring match in title/description
                if (playbook.getTriggerValue() != null) {
                        String triggerValLower = playbook.getTriggerValue().toLowerCase();
                        if ((incident.getTitle() != null && incident.getTitle().toLowerCase().contains(triggerValLower)) ||
                                        (incident.getDescription() != null && incident.getDescription().toLowerCase().contains(triggerValLower))) {
                                return true;
                        }
                }

                // 4. Dynamic name keyword match with fuzzy matching
                java.util.Set<String> stopWords = java.util.Set.of(
                                "response", "playbook", "mitigation", "containment", 
                                "detection", "automation", "remediation", "and", 
                                "or", "the", "on", "for", "action", "plan", 
                                "incident", "suspect"
                );
                
                java.util.List<String> pbKeywords = getCleanWords(playbook.getName(), stopWords);
                java.util.List<String> incWords = new java.util.ArrayList<>();
                incWords.addAll(getCleanWords(incident.getTitle(), stopWords));
                incWords.addAll(getCleanWords(incident.getDescription(), stopWords));

                for (String w1 : pbKeywords) {
                        for (String w2 : incWords) {
                                if (wordsMatchFuzzy(w1, w2)) {
                                        return true;
                                }
                        }
                }

                return false;
        }

        private String getPlaybookRelation(Playbook playbook, Incident incident) {
                if (playbook == null || incident == null) return "NONE";

                String pbName = playbook.getName() != null ? playbook.getName().toLowerCase() : "";

                java.util.Set<String> stopWords = java.util.Set.of(
                                "response", "playbook", "mitigation", "containment", 
                                "detection", "automation", "remediation", "and", 
                                "or", "the", "on", "for", "action", "plan", 
                                "incident", "suspect"
                );
                
                java.util.List<String> incWords = new java.util.ArrayList<>();
                incWords.addAll(getCleanWords(incident.getTitle(), stopWords));
                incWords.addAll(getCleanWords(incident.getDescription(), stopWords));

                boolean isVulnIncident = incWords.stream().anyMatch(w -> wordsMatchFuzzy("vulnerability", w)) 
                                || incWords.stream().anyMatch(w -> wordsMatchFuzzy("scan", w));
                boolean isLocationIncident = incWords.stream().anyMatch(w -> wordsMatchFuzzy("location", w) || wordsMatchFuzzy("login", w) || wordsMatchFuzzy("unauthorized", w) || wordsMatchFuzzy("geoip", w));
                boolean isBruteForceIncident = incWords.stream().anyMatch(w -> wordsMatchFuzzy("brute", w));
                boolean isPrivEscIncident = incWords.stream().anyMatch(w -> wordsMatchFuzzy("privilege", w));

                boolean isVulnPlaybook = pbName.contains("vulnerability") || pbName.contains("scan");
                boolean isLocationPlaybook = pbName.contains("location") || pbName.contains("unauthorized") || pbName.contains("login");
                boolean isBruteForcePlaybook = pbName.contains("brute");
                boolean isPrivEscPlaybook = pbName.contains("privilege");

                // Recommended matching
                if (isLocationIncident && isLocationPlaybook) return "RECOMMENDED";
                if (isBruteForceIncident && isBruteForcePlaybook) return "RECOMMENDED";
                if (isPrivEscIncident && isPrivEscPlaybook) return "RECOMMENDED";
                if (isVulnIncident && isVulnPlaybook && !isLocationIncident && !isBruteForceIncident && !isPrivEscIncident) {
                        return "RECOMMENDED";
                }

                // Secondary matching (Vulnerability scan is secondary for Location, Brute Force, and Privilege Escalation)
                if (isVulnPlaybook && (isLocationIncident || isBruteForceIncident || isPrivEscIncident)) {
                        return "SECONDARY";
                }

                return "NONE";
        }
}