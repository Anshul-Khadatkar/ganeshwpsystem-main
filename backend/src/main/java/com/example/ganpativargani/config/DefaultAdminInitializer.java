package com.example.ganpativargani.config;

import com.example.ganpativargani.entity.Admin;
import com.example.ganpativargani.repository.AdminRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Ensures exactly one admin account exists on first boot, sourced from
 * ADMIN_USERNAME / ADMIN_PASSWORD env vars - see .env.example. This runs
 * every startup but is a no-op once any admin row exists, so it's safe to
 * leave enabled permanently.
 */
@Slf4j
@Component
public class DefaultAdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.security.default-admin-username:}")
    private String defaultUsername;

    @Value("${app.security.default-admin-password:}")
    private String defaultPassword;

    public DefaultAdminInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.existsByDeletedFalse()) {
            return;
        }

        if (defaultUsername.isBlank() || defaultPassword.isBlank()) {
            log.warn("No admin account exists and ADMIN_USERNAME/ADMIN_PASSWORD are not set. " +
                    "Set them in .env and restart, or the app has no way to log in.");
            return;
        }

        adminRepository.save(Admin.builder()
                .username(defaultUsername)
                .passwordHash(passwordEncoder.encode(defaultPassword))
                .build());

        log.info("Created initial admin account '{}'. Change this password after first login.", defaultUsername);
    }
}
