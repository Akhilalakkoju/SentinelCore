package backend.controller;

import backend.dto.ChangePasswordRequest;
import backend.dto.ProfileDTO;
import backend.service.ProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = {
        "http://localhost:5173",
        "http://localhost:5174"
})
public class ProfileController {

    @Autowired
    private ProfileService profileService;

    @GetMapping
    public ResponseEntity<ProfileDTO> getProfile(Authentication authentication) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.getProfile(email));
    }

    @PutMapping
    public ResponseEntity<ProfileDTO> updateProfile(Authentication authentication,
                                                     @RequestBody ProfileDTO dto) {
        String email = authentication.getName();
        return ResponseEntity.ok(profileService.updateProfile(email, dto));
    }

    @PutMapping("/password")
    public ResponseEntity<Map<String, Object>> changePassword(Authentication authentication,
                                                               @RequestBody ChangePasswordRequest request) {
        String email = authentication.getName();
        profileService.changePassword(email, request);
        Map<String, Object> res = new HashMap<>();
        res.put("success", true);
        res.put("message", "Password changed successfully!");
        return ResponseEntity.ok(res);
    }
}
