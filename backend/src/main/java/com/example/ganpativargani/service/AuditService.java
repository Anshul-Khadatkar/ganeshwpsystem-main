package com.example.ganpativargani.service;

import com.example.ganpativargani.entity.AuditLog;
import com.example.ganpativargani.repository.AuditLogRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String entityName, Long entityId, String action, String details) {
        auditLogRepository.save(AuditLog.builder()
                .entityName(entityName)
                .entityId(entityId)
                .action(action)
                .performedBy(currentUser())
                .details(details)
                .build());
    }

    private String currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        // Before Session 6 wires up real authentication this safely falls back to "system".
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : "system";
    }
}
