package com.clinicapp.backend;

import com.clinicapp.backend.model.security.Role;
import com.clinicapp.backend.model.security.User;
import com.clinicapp.backend.repository.security.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
@SpringBootApplication
@RequiredArgsConstructor // Add for dependency injection
public class ClinicalManagementBackendApplication {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(ClinicalManagementBackendApplication.class, args);
	}

	@Bean
	public CommandLineRunner createDefaultAdminUser() {
		return args -> {
			String adminEmail = "admin@admin.com";
			if (userRepository.findByEmail(adminEmail).isEmpty()) {
				User adminUser = User.builder()
						.firstName("Admin")
						.lastName("User")
						.username("admin")
						.email(adminEmail)
						.password(passwordEncoder.encode("password")) // Encode the password
						.role(Role.ADMIN)
						.build();
				User doctorUser = User.builder()
						.firstName("Claudel")
						.lastName("Noubissie")
						.username("doctor")
						.email("doctor@doctor.com")
						.password(passwordEncoder.encode("password")) // Encode the password
						.role(Role.DOCTOR)
						.build();
				User secUser = User.builder()
						.firstName("Pepper")
						.lastName("Nyangono")
						.username("secretary")
						.email("sec@sec.com")
						.password(passwordEncoder.encode("password")) // Encode the password
						.role(Role.SECRETARY)
						.build();
				userRepository.save(adminUser);
				userRepository.save(doctorUser);
				userRepository.save(secUser);
				System.out.println(">>> Default admin user created: " + adminEmail);
			} else {
				System.out.println(">>> Admin user already exists.");
			}
		};
	}
}