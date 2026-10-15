package com.example.ganpativargani.whatsapp;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.whatsapp")
public record WhatsAppProperties(String provider, String webhookVerifyToken, Secrets secrets) {
    public record Secrets(
            String accessToken,
            String phoneNumberId,
            String businessAccountId,
            String apiVersion,
            String appId
    ) {
    }
}
