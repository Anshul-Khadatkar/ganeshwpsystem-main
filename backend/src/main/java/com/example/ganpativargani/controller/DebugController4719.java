package com.example.ganpativargani.controller;

import com.example.ganpativargani.service.DebugClearService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Password-gated admin debug tools. Clear password comes from DEBUG_CLEAR_PASSWORD.
 * Path is intentionally non-obvious: /api/debug-controller-4719
 */
@RestController
@RequestMapping("/api/debug-controller-4719")
@RequiredArgsConstructor
public class DebugController4719 {

    private final DebugClearService debugClearService;

    /**
     * Clears donations, contributors, broadcasts, broadcast recipients, and audit logs.
     * Does NOT delete message_templates, buildings, admins, or app_settings.
     */
    @PostMapping("/clear-data")
    public Map<String, Object> clearData(
            Authentication authentication,
            @Valid @RequestBody ClearDataRequest request
    ) {
        debugClearService.assertClearPassword(request.getPassword());
        String admin = authentication != null ? authentication.getName() : "unknown";
        Map<String, Long> deleted = debugClearService.clearAllExceptTemplates(admin);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("ok", true);
        body.put("message", "Cleared operational data. Templates, buildings, admins, and settings were kept.");
        body.put("deletedRows", deleted);
        body.put("clearedBy", admin);
        return body;
    }

    @Data
    public static class ClearDataRequest {
        @NotBlank
        private String password;
    }
}
