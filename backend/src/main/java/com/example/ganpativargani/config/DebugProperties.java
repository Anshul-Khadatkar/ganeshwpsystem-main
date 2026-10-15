package com.example.ganpativargani.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.debug")
public record DebugProperties(
        /** When blank, clear-data endpoint is disabled. */
        String clearPassword
) {}
