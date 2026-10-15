package com.example.ganpativargani.controller;

import com.example.ganpativargani.service.WhatsAppWebhookService;
import com.example.ganpativargani.whatsapp.WhatsAppProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Meta calls this endpoint directly (not the admin's browser), so it is
 * intentionally excluded from JWT auth in SecurityConfig. Security instead
 * comes from the verify token (GET) - Meta will only complete the
 * handshake if it matches WHATSAPP_WEBHOOK_VERIFY_TOKEN - and from Meta's
 * own app-secret signing on POST bodies (not verified here in v1; add
 * X-Hub-Signature-256 verification before relying on this in production
 * with sensitive data).
 */
@Slf4j
@RestController
@RequestMapping("/api/whatsapp/webhook")
public class WhatsAppWebhookController {

    private final WhatsAppProperties properties;
    private final WhatsAppWebhookService webhookService;

    public WhatsAppWebhookController(WhatsAppProperties properties, WhatsAppWebhookService webhookService) {
        this.properties = properties;
        this.webhookService = webhookService;
    }

    /** Meta's one-time verification handshake when you click "Verify and save" in the dashboard. */
    @GetMapping
    public ResponseEntity<String> verify(
            @RequestParam("hub.mode") String mode,
            @RequestParam("hub.verify_token") String verifyToken,
            @RequestParam("hub.challenge") String challenge
    ) {
        boolean tokenConfigured = properties.webhookVerifyToken() != null && !properties.webhookVerifyToken().isBlank();

        if (tokenConfigured && "subscribe".equals(mode) && properties.webhookVerifyToken().equals(verifyToken)) {
            return ResponseEntity.ok(challenge);
        }
        log.warn("WhatsApp webhook verification failed (mode={})", mode);
        return ResponseEntity.status(403).build();
    }

    /** Ongoing delivery-status callbacks (sent/delivered/read/failed) and inbound messages. */
    @PostMapping
    public ResponseEntity<Void> receive(@RequestBody Map<String, Object> payload) {
        webhookService.processPayload(payload);
        // Always 200 quickly - Meta retries aggressively on non-2xx responses.
        return ResponseEntity.ok().build();
    }
}
