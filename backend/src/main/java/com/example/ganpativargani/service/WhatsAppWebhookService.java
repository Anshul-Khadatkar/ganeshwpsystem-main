package com.example.ganpativargani.service;

import com.example.ganpativargani.entity.BroadcastRecipient;
import com.example.ganpativargani.entity.Donation;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.repository.BroadcastRecipientRepository;
import com.example.ganpativargani.repository.DonationRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Handles Meta's asynchronous delivery-status callbacks (sent/delivered/
 * read/failed) and reconciles them onto whichever record originally sent
 * the message - a donation thank-you or a broadcast recipient - matched by
 * {@code providerMessageId}. This is what keeps WhatsApp status accurate
 * after the initial synchronous send (e.g. a message can be accepted by
 * Meta immediately but reported "failed" moments later).
 * <p>
 * Best-effort by design: webhook payloads reference messages this app may
 * not recognize (e.g. inbound replies), which are safely ignored.
 */
@Slf4j
@Service
public class WhatsAppWebhookService {

    private final DonationRepository donationRepository;
    private final BroadcastRecipientRepository broadcastRecipientRepository;

    public WhatsAppWebhookService(DonationRepository donationRepository,
                                   BroadcastRecipientRepository broadcastRecipientRepository) {
        this.donationRepository = donationRepository;
        this.broadcastRecipientRepository = broadcastRecipientRepository;
    }

    @Transactional
    @SuppressWarnings("unchecked")
    public void processPayload(Map<String, Object> payload) {
        try {
            var entries = (List<Map<String, Object>>) payload.getOrDefault("entry", List.of());
            for (Map<String, Object> entry : entries) {
                var changes = (List<Map<String, Object>>) entry.getOrDefault("changes", List.of());
                for (Map<String, Object> change : changes) {
                    var value = (Map<String, Object>) change.get("value");
                    if (value == null) continue;
                    var statuses = (List<Map<String, Object>>) value.getOrDefault("statuses", List.of());
                    for (Map<String, Object> status : statuses) {
                        applyStatus(
                                String.valueOf(status.get("id")),
                                String.valueOf(status.get("status")),
                                formatFailureReason(status)
                        );
                    }
                }
            }
        } catch (Exception ex) {
            // Never let a malformed/unexpected payload shape throw a 500 back to Meta -
            // that would cause Meta to retry indefinitely. Log and move on.
            log.warn("Failed to process WhatsApp webhook payload", ex);
        }
    }

    private void applyStatus(String providerMessageId, String metaStatus, String failureReason) {
        if (providerMessageId == null || "null".equals(providerMessageId)) {
            return;
        }

        // "failed" is the only status that should ever move a record backward from SENT.
        // sent/delivered/read all mean "still fine" - a donation/recipient already marked
        // SENT after the synchronous send stays SENT.
        if (!"failed".equalsIgnoreCase(metaStatus)) {
            return;
        }

        String reason = (failureReason == null || failureReason.isBlank())
                ? "Delivery failed (reported by WhatsApp after send)"
                : failureReason;

        donationRepository.findByWhatsappMessageId(providerMessageId)
                .ifPresent(donation -> markDonationFailed(donation, reason));
        broadcastRecipientRepository.findByProviderMessageId(providerMessageId)
                .ifPresent(recipient -> markRecipientFailed(recipient, reason));
    }

    @SuppressWarnings("unchecked")
    private String formatFailureReason(Map<String, Object> status) {
        var errors = (List<Map<String, Object>>) status.getOrDefault("errors", List.of());
        if (errors.isEmpty()) {
            return null;
        }
        Map<String, Object> first = errors.get(0);
        String code = String.valueOf(first.getOrDefault("code", ""));
        String message = String.valueOf(first.getOrDefault("message", first.getOrDefault("title", "")));
        if ("null".equals(message) || message.isBlank()) {
            return null;
        }
        if (!"null".equals(code) && !code.isBlank()) {
            log.warn("WhatsApp delivery failed (Meta error {}): {}", code, message);
            return "WhatsApp error " + code + ": " + message;
        }
        log.warn("WhatsApp delivery failed: {}", message);
        return message;
    }

    private void markDonationFailed(Donation donation, String reason) {
        donation.setWhatsappStatus(WhatsAppStatus.FAILED);
        donation.setWhatsappErrorMessage(reason);
        donationRepository.save(donation);
    }

    private void markRecipientFailed(BroadcastRecipient recipient, String reason) {
        recipient.setStatus(WhatsAppStatus.FAILED);
        recipient.setErrorMessage(reason);
        broadcastRecipientRepository.save(recipient);
    }
}
