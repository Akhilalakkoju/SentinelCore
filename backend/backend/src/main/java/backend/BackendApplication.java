package backend;

import backend.entity.Role;
import backend.entity.User;
import backend.repository.RoleRepository;
import backend.repository.UserRepository;
import backend.service.VulnerabilityService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;


@SpringBootApplication
@EnableScheduling
public class BackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendApplication.class, args);
	}

	@Bean
	@SuppressWarnings("null")
	CommandLineRunner ensureDefaultData(
			RoleRepository roleRepository,
			UserRepository userRepository,
			PasswordEncoder passwordEncoder,
			VulnerabilityService vulnerabilityService) {
		return args -> {
			// Seed Roles
			for (String roleName : new String[]{"ADMIN", "ANALYST"}) {
				if (roleRepository.findByName(roleName).isEmpty()) {
					roleRepository.save(Role.builder().name(roleName).build());
				}
			}

			if (roleRepository.findByName("VIEWER").isEmpty()) {
				Role viewerRole = roleRepository.findByName("USER")
						.orElseGet(() -> Role.builder().build());
				viewerRole.setName("VIEWER");
				roleRepository.save(viewerRole);
			}

			// Seed Default Admin Accounts if missing
			Role adminRole = roleRepository.findByName("ADMIN").orElse(null);

			if (adminRole != null) {
				// 1. admin@sentinelcore.com
				if (userRepository.findByEmail("admin@sentinelcore.com").isEmpty()) {
					User adminUser = new User();
					adminUser.setName("System Administrator");
					adminUser.setEmail("admin@sentinelcore.com");
					adminUser.setPassword(passwordEncoder.encode("password123"));
					adminUser.setRole(adminRole);
					adminUser.setEnabled(true);
					userRepository.save(adminUser);
				}

				// 2. admin@admin.com
				if (userRepository.findByEmail("admin@admin.com").isEmpty()) {
					User adminUser2 = new User();
					adminUser2.setName("Admin User");
					adminUser2.setEmail("admin@admin.com");
					adminUser2.setPassword(passwordEncoder.encode("password123"));
					adminUser2.setRole(adminRole);
					adminUser2.setEnabled(true);
					userRepository.save(adminUser2);
				}
			}

			if (vulnerabilityService.getAllVulnerabilities().isEmpty()) {
				vulnerabilityService.triggerScan();
			}
		};
	}

}
