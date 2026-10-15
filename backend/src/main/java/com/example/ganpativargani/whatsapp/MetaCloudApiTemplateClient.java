package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MetaParameterFormat;
import com.example.ganpativargani.entity.MetaTemplateStatus;
import com.example.ganpativargani.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "provider", havingValue = "meta-cloud-api")
public class MetaCloudApiTemplateClient implements MetaTemplateClient {

    private final WhatsAppProperties properties;
    private final RestClient restClient;
    private final MetaMediaUploader mediaUploader;

    public MetaCloudApiTemplateClient(WhatsAppProperties properties, MetaMediaUploader mediaUploader) {
        this.properties = properties;
        this.mediaUploader = mediaUploader;
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .defaultHeader("Authorization", "Bearer " + properties.secrets().accessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    private String templatesPath() {
        return "/%s/%s/message_templates".formatted(
                properties.secrets().apiVersion(),
                properties.secrets().businessAccountId());
    }

    @Override
    public MetaTemplateSyncResult submit(MessageTemplate template) {
        String apiName = resolveApiName(template);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("name", apiName);
        payload.put("language", template.getMetaLanguageCode());
        payload.put("category", template.getMetaCategory().name().toLowerCase());
        if (template.getMetaParameterFormat() == MetaParameterFormat.NAMED) {
            payload.put("parameter_format", "named");
        }

        try {
            List<Map<String, Object>> components = new ArrayList<>();
            boolean withImage = template.getImageUrl() != null && !template.getImageUrl().isBlank();
            if (withImage) {
                String handle = mediaUploader.uploadImageFromUrl(template.getImageUrl());
                components.add(MetaTemplateBodyBuilder.buildImageHeaderComponent(handle));
                template.setMetaHasImageHeader(true);
            } else {
                template.setMetaHasImageHeader(false);
            }
            components.add(MetaTemplateBodyBuilder.buildBodyComponent(
                    template.getMessage(), template.getMetaParameterFormat()));
            payload.put("components", components);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException(ex.getMessage());
        }

        String path = templatesPath();
        try {
            String raw = restClient.post()
                    .uri(path)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            MetaGraphJson.parseMap(raw);
            log.info("Submitted WhatsApp template '{}' to Meta for review", apiName);
            return new MetaTemplateSyncResult(MetaTemplateStatus.PENDING, null);
        } catch (RestClientException ex) {
            log.warn("Meta template submission failed for '{}'", apiName, ex);
            throw new BadRequestException("Meta rejected the template submission. "
                    + extractErrorMessage(ex)
                    + " Check that your access token has whatsapp_business_management permission.");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public MetaTemplateSyncResult fetchStatus(String templateName, String languageCode) {
        String path = templatesPath();
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("fields", "name,status,rejected_reason,language")
                            .queryParam("name", templateName)
                            .build())
                    .retrieve()
                    .body(String.class);
            Map<String, Object> response = MetaGraphJson.parseMap(raw);

            if (!response.containsKey("data")) {
                return new MetaTemplateSyncResult(MetaTemplateStatus.NOT_SUBMITTED, null);
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> row : data) {
                if (!templateName.equals(row.get("name"))) {
                    continue;
                }
                String lang = String.valueOf(row.get("language"));
                if (languageCode != null && !languageCode.equals(lang)) {
                    continue;
                }
                return mapStatus(row);
            }
            return new MetaTemplateSyncResult(MetaTemplateStatus.NOT_SUBMITTED, null);
        } catch (RestClientException ex) {
            log.warn("Failed to fetch Meta template status for '{}'", templateName, ex);
            throw new BadRequestException("Could not fetch template status from Meta. " + extractErrorMessage(ex));
        }
    }

    private MetaTemplateSyncResult mapStatus(Map<String, Object> row) {
        String status = String.valueOf(row.get("status")).toUpperCase();
        String rejectionReason = row.get("rejected_reason") != null
                ? String.valueOf(row.get("rejected_reason"))
                : null;

        MetaTemplateStatus mapped = switch (status) {
            case "APPROVED" -> MetaTemplateStatus.APPROVED;
            case "REJECTED" -> MetaTemplateStatus.REJECTED;
            case "PENDING", "IN_APPEAL" -> MetaTemplateStatus.PENDING;
            default -> MetaTemplateStatus.PENDING;
        };
        return new MetaTemplateSyncResult(mapped, rejectionReason);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Optional<MetaRemoteTemplate> findRemote(String templateName, String languageCode) {
        String path = templatesPath();
        try {
            String raw = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("fields", "name,status,language,category,components,rejected_reason")
                            .queryParam("name", templateName)
                            .build())
                    .retrieve()
                    .body(String.class);
            Map<String, Object> response = MetaGraphJson.parseMap(raw);

            if (!response.containsKey("data")) {
                return Optional.empty();
            }

            List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
            for (Map<String, Object> row : data) {
                if (!templateName.equals(row.get("name"))) {
                    continue;
                }
                String lang = String.valueOf(row.get("language"));
                if (languageCode != null && !languageCode.equals(lang)) {
                    continue;
                }
                return Optional.of(parseRemoteRow(row));
            }
            return Optional.empty();
        } catch (RestClientException ex) {
            log.warn("Failed to fetch Meta template '{}'", templateName, ex);
            throw new BadRequestException("Could not fetch template from Meta. " + extractErrorMessage(ex));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<MetaRemoteTemplate> listFromMeta(boolean approvedOnly) {
        String path = templatesPath();
        List<MetaRemoteTemplate> results = new ArrayList<>();
        String after = null;

        try {
            do {
                final String cursor = after;
                String raw = restClient.get()
                        .uri(uriBuilder -> {
                            var builder = uriBuilder
                                    .path(path)
                                    .queryParam("fields", "name,status,language,category,components,rejected_reason")
                                    .queryParam("limit", "100");
                            if (cursor != null) {
                                builder.queryParam("after", cursor);
                            }
                            return builder.build();
                        })
                        .retrieve()
                        .body(String.class);
                Map<String, Object> response = MetaGraphJson.parseMap(raw);

                if (!response.containsKey("data")) {
                    break;
                }

                List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
                for (Map<String, Object> row : data) {
                    MetaRemoteTemplate parsed = parseRemoteRow(row);
                    if (!approvedOnly || parsed.status() == MetaTemplateStatus.APPROVED) {
                        results.add(parsed);
                    }
                }

                after = extractNextCursor(response);
            } while (after != null);

            return results;
        } catch (RestClientException ex) {
            log.warn("Failed to list Meta templates", ex);
            throw new BadRequestException("Could not fetch templates from Meta. " + extractErrorMessage(ex)
                    + " Ensure your token has whatsapp_business_management permission.");
        }
    }

    @SuppressWarnings("unchecked")
    private MetaRemoteTemplate parseRemoteRow(Map<String, Object> row) {
        String name = String.valueOf(row.get("name"));
        String language = String.valueOf(row.get("language"));
        MetaTemplateStatus status = mapStatus(row).status();
        String rejectionReason = row.get("rejected_reason") != null
                ? String.valueOf(row.get("rejected_reason"))
                : null;

        com.example.ganpativargani.entity.MetaTemplateCategory category =
                com.example.ganpativargani.entity.MetaTemplateCategory.UTILITY;
        if (row.get("category") != null) {
            try {
                category = com.example.ganpativargani.entity.MetaTemplateCategory.valueOf(
                        String.valueOf(row.get("category")).toUpperCase());
            } catch (IllegalArgumentException ignored) {
                // keep default
            }
        }

        String bodyText = extractBodyText(row.get("components"));
        boolean hasImageHeader = hasImageHeader(row.get("components"));
        return new MetaRemoteTemplate(name, language, status, category, bodyText, rejectionReason, hasImageHeader);
    }

    @SuppressWarnings("unchecked")
    private boolean hasImageHeader(Object componentsObj) {
        if (!(componentsObj instanceof List<?> components)) {
            return false;
        }
        for (Object item : components) {
            if (item instanceof Map<?, ?> component) {
                if ("HEADER".equalsIgnoreCase(String.valueOf(component.get("type")))
                        && "IMAGE".equalsIgnoreCase(String.valueOf(component.get("format")))) {
                    return true;
                }
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private String extractBodyText(Object componentsObj) {
        if (!(componentsObj instanceof List<?> components)) {
            return "";
        }
        for (Object item : components) {
            if (item instanceof Map<?, ?> component) {
                if ("BODY".equalsIgnoreCase(String.valueOf(component.get("type")))) {
                    Object text = component.get("text");
                    return text != null ? String.valueOf(text) : "";
                }
            }
        }
        return "";
    }

    @SuppressWarnings("unchecked")
    private String extractNextCursor(Map<String, Object> response) {
        Object pagingObj = response.get("paging");
        if (!(pagingObj instanceof Map<?, ?> paging)) {
            return null;
        }
        Object cursorsObj = paging.get("cursors");
        if (!(cursorsObj instanceof Map<?, ?> cursors)) {
            return null;
        }
        Object next = cursors.get("after");
        if (next == null || String.valueOf(next).isBlank()) {
            return null;
        }
        List<Map<String, Object>> data = (List<Map<String, Object>>) response.get("data");
        if (data == null || data.isEmpty()) {
            return null;
        }
        return String.valueOf(next);
    }

    private String resolveApiName(MessageTemplate template) {
        if (template.getMetaTemplateName() != null && !template.getMetaTemplateName().isBlank()) {
            return template.getMetaTemplateName().trim();
        }
        return MetaTemplateBodyBuilder.toMetaApiName(template.getName());
    }

    @Override
    public void deleteByName(String templateName) {
        if (templateName == null || templateName.isBlank()) {
            throw new BadRequestException("Meta template name is required to delete from Meta.");
        }
        String path = templatesPath();
        try {
            String raw = restClient.delete()
                    .uri(uriBuilder -> uriBuilder
                            .path(path)
                            .queryParam("name", templateName.trim())
                            .build())
                    .retrieve()
                    .body(String.class);
            log.info("Deleted WhatsApp template '{}' from Meta. response={}", templateName, raw);
        } catch (RestClientException ex) {
            String detail = extractErrorMessage(ex);
            String lower = detail.toLowerCase();
            // Already gone on Meta — treat as success so local soft-delete can proceed
            if (lower.contains("does not exist")
                    || lower.contains("not found")
                    || (detail.contains("100") && lower.contains("object"))) {
                log.info("Meta template '{}' already absent; continuing local delete. detail={}", templateName, detail);
                return;
            }
            log.warn("Meta template delete failed for '{}'", templateName, ex);
            throw new BadRequestException("Could not delete template from Meta. " + detail);
        }
    }

    private String extractErrorMessage(RestClientException ex) {
        String msg = ex.getMessage();
        if (msg == null) {
            return "Unknown error.";
        }
        if (msg.length() > 300) {
            return msg.substring(0, 300) + "…";
        }
        return msg;
    }
}
