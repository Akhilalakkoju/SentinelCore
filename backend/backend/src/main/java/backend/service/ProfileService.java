package backend.service;

import backend.dto.ChangePasswordRequest;
import backend.dto.ProfileDTO;
import backend.entity.User;
import backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public ProfileDTO getProfile(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        String roleName = user.getRole() != null ? user.getRole().getName() : "USER";
        return new ProfileDTO(
                user.getName(),
                user.getEmail(),
                roleName,
                user.getProfileImage(),
                user.getTheme() != null ? user.getTheme() : "dark",
                user.getEmailNotificationsEnabled(),
                user.getInAppNotificationsEnabled()
        );
    }

    public ProfileDTO updateProfile(String email, ProfileDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            user.setName(dto.getName().trim());
        }

        if (dto.getProfileImage() != null) {
            user.setProfileImage(dto.getProfileImage());
        }

        if (dto.getTheme() != null && (dto.getTheme().equalsIgnoreCase("light") || dto.getTheme().equalsIgnoreCase("dark"))) {
            user.setTheme(dto.getTheme().toLowerCase());
        }

        if (dto.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(dto.getEmailNotificationsEnabled());
        }

        if (dto.getInAppNotificationsEnabled() != null) {
            user.setInAppNotificationsEnabled(dto.getInAppNotificationsEnabled());
        }

        userRepository.save(user);

        return getProfile(email);
    }

    public void changePassword(String email, ChangePasswordRequest request) {
        if (request.getOldPassword() == null || request.getOldPassword().isEmpty()) {
            throw new IllegalArgumentException("Current (Old) Password is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            throw new IllegalArgumentException("New password must be at least 8 characters long");
        }

        if (request.getConfirmPassword() == null || !request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Confirm Password does not match New Password");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect Old Password");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
