package com.example.ganpativargani.service;

import com.example.ganpativargani.config.DebugProperties;
import com.example.ganpativargani.exception.BadRequestException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class DebugClearService {

    private static final Logger log = LoggerFactory.getLogger(DebugClearService.class);

    private final DebugProperties debugProperties;

    @PersistenceContext
    private EntityManager entityManager;

    public DebugClearService(DebugProperties debugProperties) {
        this.debugProperties = debugProperties;
    }

    public void assertClearPassword(String provided) {
        String expected = debugProperties.clearPassword();
        if (expected == null || expected.isBlank()) {
            throw new BadRequestException("Debug clear is disabled (DEBUG_CLEAR_PASSWORD not set)");
        }
        if (provided == null || provided.isBlank() || !constantTimeEquals(expected, provided)) {
            throw new BadRequestException("Invalid debug password");
        }
    }

    /**
     * Hard-deletes operational data. Keeps message_templates, buildings, admins, app_settings.
     */
    @Transactional
    public Map<String, Long> clearAllExceptTemplates(String adminUsername) {
        Map<String, Long> deleted = new LinkedHashMap<>();
        deleted.put("broadcast_recipients", deleteFrom("broadcast_recipients"));
        deleted.put("broadcasts", deleteFrom("broadcasts"));
        deleted.put("donations", deleteFrom("donations"));
        deleted.put("contributors", deleteFrom("contributors"));
        deleted.put("audit_logs", deleteFrom("audit_logs"));

        log.warn(
                "Debug clear executed by admin={} kept=message_templates,buildings,admins,app_settings deleted={}",
                adminUsername,
                deleted
        );
        return deleted;
    }

    private long deleteFrom(String table) {
        return entityManager.createNativeQuery("DELETE FROM " + table).executeUpdate();
    }

    private static boolean constantTimeEquals(String a, String b) {
        byte[] left = a.getBytes(StandardCharsets.UTF_8);
        byte[] right = b.getBytes(StandardCharsets.UTF_8);
        return MessageDigest.isEqual(left, right);
    }
}
