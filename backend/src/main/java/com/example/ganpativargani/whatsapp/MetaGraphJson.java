package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.exception.BadRequestException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

/**
 * Meta's Graph API returns JSON payloads with {@code Content-Type: text/javascript},
 * which Spring's default {@code RestClient} cannot deserialize to {@code Map}.
 * Always read the raw body as {@code String} and parse it here.
 */
public final class MetaGraphJson {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private MetaGraphJson() {
    }

    public static Map<String, Object> parseMap(String body) {
        if (body == null || body.isBlank()) {
            return Map.of();
        }
        try {
            return MAPPER.readValue(body, new TypeReference<>() {});
        } catch (Exception ex) {
            throw new BadRequestException("Unexpected response from Meta API. Could not parse JSON.");
        }
    }
}
