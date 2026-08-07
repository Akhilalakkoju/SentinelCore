package backend.service;

import backend.dto.JwtResponse;
import backend.dto.RegisterRequest;
import backend.entity.Role;
import backend.entity.User;
import backend.entity.RefreshToken;
import backend.repository.RoleRepository;
import backend.repository.UserRepository;
import backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuditLogService auditLogService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private RefreshTokenService refreshTokenService;

    // ===========================
    // LOGIN
    // ===========================
    public JwtResponse login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User Not Found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid Password");
        }

        String role = user.getRole().getName();

        String token = jwtUtil.generateToken(
                user.getEmail(),
                role
        );

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        auditLogService.createLog("LOGIN", "User " + user.getEmail() + " logged in successfully", user, null);

        return new JwtResponse(
                token,
                refreshToken.getToken(),
                user.getEmail(),
                role,
                "Login Successful"
        );
    }

    @Autowired
    private SettingsService settingsService;

    // ===========================
    // REGISTER
    // ===========================
    public String register(RegisterRequest request) {

        // Check password minimum length (default 8)
        int minLength = 8;
        if (request.getPassword() == null || request.getPassword().length() < minLength) {
            throw new RuntimeException("Password must be at least " + minLength + " characters.");
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already registered");
        }

        // Resolve requested role name (default to ANALYST)
        String reqRole = request.getRoleName();
        if (reqRole == null || reqRole.trim().isEmpty()) {
            reqRole = "ANALYST";
        }
        reqRole = reqRole.trim().toUpperCase();

        // Verify secret passwords for privileged roles
        if ("ADMIN".equals(reqRole)) {
            if (request.getSecretPassword() == null || !request.getSecretPassword().equals("Admin@only")) {
                throw new RuntimeException("Invalid secret password for Admin registration.");
            }
        } else if ("ANALYST".equals(reqRole)) {
            if (request.getSecretPassword() == null || !request.getSecretPassword().equals("Analyst@only")) {
                throw new RuntimeException("Invalid secret password for Analyst registration.");
            }
        }

        // Assign resolved role
        final String finalRoleName = reqRole;
        Role role = roleRepository.findByName(finalRoleName)
                .orElseThrow(() -> new RuntimeException("Role " + finalRoleName + " not found"));

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        user.setEnabled(true);

        User saved = userRepository.save(user);

        auditLogService.createLog("REGISTER", "New " + reqRole.toLowerCase() + " account created: " + saved.getEmail(), saved, null);

        return "Registration Successful";
    }
}