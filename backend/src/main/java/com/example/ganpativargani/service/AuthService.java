package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.AuthDtos.LoginRequest;
import com.example.ganpativargani.dto.AuthDtos.LoginResponse;
import com.example.ganpativargani.dto.AuthDtos.ChangePasswordRequest;
import com.example.ganpativargani.entity.Admin;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.repository.AdminRepository;
import com.example.ganpativargani.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(AdminRepository adminRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Admin admin = adminRepository.findByUsernameAndDeletedFalse(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
            throw new BadRequestException("Invalid username or password");
        }

        return LoginResponse.builder()
                .token(jwtService.issueToken(admin.getUsername()))
                .username(admin.getUsername())
                .build();
    }

    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        Admin admin = adminRepository.findByUsernameAndDeletedFalse(username)
                .orElseThrow(() -> new BadRequestException("Admin not found"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), admin.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }
        if (request.getNewPassword().length() < 8) {
            throw new BadRequestException("New password must be at least 8 characters");
        }

        admin.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        adminRepository.save(admin);
    }

    /** Invalidate the current JWT so it cannot be reused after logout. */
    public void logout(String token) {
        if (token != null && !token.isBlank()) {
            jwtService.revokeToken(token);
        }
    }
}
