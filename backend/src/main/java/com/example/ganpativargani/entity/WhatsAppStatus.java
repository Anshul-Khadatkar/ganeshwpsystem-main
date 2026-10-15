package com.example.ganpativargani.entity;

/**
 * Lifecycle of an outbound WhatsApp message, shared by donation
 * thank-you sends and broadcast recipients.
 *
 * NOT_SENT   - no send was ever requested (e.g. checkbox left unticked)
 * PENDING    - queued / request accepted by provider, awaiting delivery confirmation
 * SENT       - provider accepted and (for providers with webhooks) confirmed delivery
 * FAILED     - provider rejected the send or delivery failed
 */
public enum WhatsAppStatus {
    NOT_SENT,
    PENDING,
    SENT,
    FAILED
}
