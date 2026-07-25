package backend.service;

import backend.entity.Threat;
import backend.entity.User;
import backend.repository.ThreatRepository;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@SuppressWarnings("null")
public class ThreatService {

    @Autowired
    private ThreatRepository threatRepository;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }

    // Get all threats
    public List<Threat> getAllThreats() {
        return threatRepository.findAll();
    }

    // Get threat by ID
    public Threat getThreatById(Long id) {
        return threatRepository.findById(id).orElse(null);
    }

    // Add new threat
    public Threat saveThreat(Threat threat) {
        Threat saved = threatRepository.save(threat);
        auditLogService.createLog("CREATE_THREAT",
                "New threat added: " + saved.getTitle() + " (Severity: " + saved.getSeverity() + ")",
                getCurrentUser(), null);
        return saved;
    }

    // Update existing threat
    public Threat updateThreat(Long id, Threat updatedThreat) {
        Threat existingThreat = threatRepository.findById(id).orElse(null);
        if (existingThreat == null) return null;

        existingThreat.setTitle(updatedThreat.getTitle());
        existingThreat.setSeverity(updatedThreat.getSeverity());
        existingThreat.setSource(updatedThreat.getSource());
        existingThreat.setStatus(updatedThreat.getStatus());

        Threat saved = threatRepository.save(existingThreat);
        auditLogService.createLog("UPDATE_THREAT",
                "Threat updated: " + saved.getTitle() + " (Status: " + saved.getStatus() + ")",
                getCurrentUser(), null);
        return saved;
    }

    // Delete threat
    public void deleteThreat(Long id) {
        Threat threat = threatRepository.findById(id).orElse(null);
        if (threat != null) {
            threatRepository.delete(threat);
            auditLogService.createLog("DELETE_THREAT",
                    "Threat deleted: " + threat.getTitle(),
                    getCurrentUser(), null);
        }
    }
}