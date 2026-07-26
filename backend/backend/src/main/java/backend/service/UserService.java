package backend.service;

import backend.entity.Role;
import backend.entity.User;
import backend.repository.AuditLogRepository;
import backend.repository.IncidentRepository;
import backend.repository.KnowledgeBaseRevisionRepository;
import backend.repository.PlaybookAuditLogRepository;
import backend.repository.PlaybookExecutionRepository;
import backend.repository.RefreshTokenRepository;
import backend.repository.RoleRepository;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import backend.service.AuditLogService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
@SuppressWarnings("null")
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PlaybookAuditLogRepository playbookAuditLogRepository;

    @Autowired
    private PlaybookExecutionRepository playbookExecutionRepository;

    @Autowired
    private KnowledgeBaseRevisionRepository kbRevisionRepository;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        String email = authentication.getName();
        return userRepository.findByEmail(email).orElse(null);
    }

    // ================= Get All Users =================

    public List<User> getAllUsers() {

        return userRepository.findAll();

    }

    // ================= Get User By ID =================

    public User getUserById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User Not Found"));

    }

    // ================= Add User =================

    public User addUser(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Encrypt Password
        user.setPassword(
                passwordEncoder.encode(user.getPassword())
        );

        // Load Role
        Role role = roleRepository.findById(user.getRole().getId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        user.setRole(role);

        // Enable user by default
        user.setEnabled(true);

        User saved = userRepository.save(user);

        auditLogService.createLog("CREATE_USER", "New analyst account created: " + saved.getEmail() + " with role " + saved.getRole().getName(), getCurrentUser(), null);

        return saved;

    }

    // ================= Update User =================

    public User updateUser(Long id, User updatedUser) {

        User user = getUserById(id);

        user.setName(updatedUser.getName());
        user.setEmail(updatedUser.getEmail());

        // Update Password only if entered
        if (updatedUser.getPassword() != null &&
                !updatedUser.getPassword().isBlank()) {

            user.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );

        }

        String oldRoleName = user.getRole().getName();

        // Update Role
        Role role = roleRepository.findById(updatedUser.getRole().getId())
                .orElseThrow(() ->
                        new RuntimeException("Role not found"));

        user.setRole(role);

        // Update Status
        user.setEnabled(updatedUser.getEnabled());

        User saved = userRepository.save(user);

        if (!oldRoleName.equalsIgnoreCase(role.getName())) {
            auditLogService.createLog("ROLE_CHANGED", "User role changed from " + oldRoleName + " to " + role.getName() + " for user " + saved.getEmail(), getCurrentUser(), null);
        } else {
            auditLogService.createLog("UPDATE_USER", "User account " + saved.getEmail() + " details updated", getCurrentUser(), null);
        }

        return saved;

    }

    // ================= Delete User =================

    @Transactional
    public void deleteUser(Long id) {

        User user = getUserById(id);

        // 1. Nullify audit_log.user_id references
        auditLogRepository.nullifyUserReferences(id);

        // 2. Unassign from incidents
        incidentRepository.findByAssignedToId(id).forEach(incident -> {
            incident.setAssignedTo(null);
            incidentRepository.save(incident);
        });

        // 3. Delete refresh token (hard FK, must delete not null)
        refreshTokenRepository.deleteByUser(user);

        // 4. Nullify playbook_audit_logs.performed_by_id references
        playbookAuditLogRepository.nullifyPerformedByReferences(id);

        // 5. Nullify playbook_executions.triggered_by_id references
        playbookExecutionRepository.nullifyTriggeredByReferences(id);

        // 6. Nullify kb_article_revisions.updated_by_id references
        kbRevisionRepository.nullifyUpdatedByReferences(id);

        // 7. Log the deletion
        auditLogService.createLog("DELETE_USER",
                "User account " + user.getEmail() + " (" + user.getName() + ") deleted",
                getCurrentUser(), null);

        // 8. Now safe to delete
        userRepository.delete(user);

    }

}