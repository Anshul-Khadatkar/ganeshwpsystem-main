package com.example.ganpativargani.whatsapp;

/**
 * A single outbound WhatsApp message, already fully resolved (variables
 * substituted, image URL decided). Neither this record nor anything that
 * builds it knows or cares whether the message came from a donation
 * thank-you, a broadcast, or an ad-hoc send - that's the point.
 */
public record WhatsAppMessageRequest(
        String toPhoneNumber,
        String message,
        String imageUrl // nullable - text-only when absent
) {
}
