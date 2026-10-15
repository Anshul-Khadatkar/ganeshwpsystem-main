package com.example.ganpativargani.whatsapp;

public record WhatsAppSendResult(
        boolean success,
        String providerMessageId, // null on failure
        String errorMessage       // null on success
) {
    public static WhatsAppSendResult success(String providerMessageId) {
        return new WhatsAppSendResult(true, providerMessageId, null);
    }

    public static WhatsAppSendResult failure(String errorMessage) {
        return new WhatsAppSendResult(false, null, errorMessage);
    }
}
