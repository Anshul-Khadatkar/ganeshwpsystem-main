package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaParameterFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Real Meta WhatsApp Cloud API implementation.
 * <p>
 * Two distinct send paths, matching Meta's 24-hour customer service window
 * rule (verified against current Meta documentation as of this build):
 * <ul>
 *   <li>{@link #send} - free-form text/image. Only deliverable to a
 *       recipient who has messaged your business number within the last
 *       24 hours.</li>
 *   <li>{@link #sendTemplate} - a Meta-approved Message Template
 *       (created and approved in Meta Business Manager - a different
 *       concept from this app's own {@link com.example.ganpativargani.entity.MessageTemplate}
 *       entity). Deliverable to any recipient, any time, since Meta has
 *       already vetted the content. This is the only reliable path for
 *       donation thank-yous and festival announcements sent to
 *       contributors who haven't messaged the business first.</li>
 * </ul>
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "provider", havingValue = "meta-cloud-api")
public class MetaCloudApiWhatsAppProvider implements WhatsAppProvider {

    private final WhatsAppProperties properties;
    private final RestClient restClient;

    public MetaCloudApiWhatsAppProvider(WhatsAppProperties properties) {
        this.properties = properties;
        this.restClient = RestClient.builder()
                .baseUrl("https://graph.facebook.com")
                .defaultHeader("Authorization", "Bearer " + properties.secrets().accessToken())
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    @Override
    public WhatsAppSendResult send(WhatsAppMessageRequest request) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", WhatsAppPhoneNormalizer.forMeta(request.toPhoneNumber()));

        if (request.imageUrl() != null && !request.imageUrl().isBlank()) {
            payload.put("type", "image");
            payload.put("image", Map.of("link", request.imageUrl(), "caption", request.message()));
        } else {
            payload.put("type", "text");
            payload.put("text", Map.of("body", request.message(), "preview_url", false));
        }

        return post(payload, "The recipient may be outside the 24-hour messaging window, " +
                "which requires an approved message template.");
    }

    @Override
    public WhatsAppSendResult sendTemplate(String toPhoneNumber, String templateName, String languageCode,
                                            MetaParameterFormat parameterFormat, Map<String, String> namedParameters,
                                            String headerImageUrl) {
        List<Map<String, Object>> bodyParameters = new ArrayList<>();

        for (Map.Entry<String, String> entry : namedParameters.entrySet()) {
            // Meta rejects empty/blank template parameters outright, so the caller-supplied
            // "-" placeholder for missing values (see TemplateVariableResolver) is essential here.
            String value = (entry.getValue() == null || entry.getValue().isBlank()) ? "\u2014" : entry.getValue();

            Map<String, Object> param = new LinkedHashMap<>();
            param.put("type", "text");
            param.put("text", value);
            if (parameterFormat == MetaParameterFormat.NAMED) {
                param.put("parameter_name", entry.getKey());
            }
            bodyParameters.add(param);
        }

        List<Map<String, Object>> components = new ArrayList<>();
        if (headerImageUrl != null && !headerImageUrl.isBlank()) {
            components.add(Map.of(
                    "type", "header",
                    "parameters", List.of(Map.of(
                            "type", "image",
                            "image", Map.of("link", headerImageUrl.trim())
                    ))
            ));
        }
        components.add(Map.of("type", "body", "parameters", bodyParameters));

        Map<String, Object> template = new LinkedHashMap<>();
        template.put("name", templateName);
        template.put("language", Map.of("code", languageCode));
        template.put("components", components);

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("messaging_product", "whatsapp");
        payload.put("to", WhatsAppPhoneNormalizer.forMeta(toPhoneNumber));
        payload.put("type", "template");
        payload.put("template", template);

        return post(payload, "Template send failed. Double-check the template name, language code, "
                + "image header (if any), and that parameters match what Meta approved.");
    }

    private WhatsAppSendResult post(Map<String, Object> payload, String failureHint) {
        String path = "/%s/%s/messages".formatted(properties.secrets().apiVersion(), properties.secrets().phoneNumberId());
        try {
            String raw = restClient.post()
                    .uri(path)
                    .body(payload)
                    .retrieve()
                    .body(String.class);
            Map<String, Object> response = MetaGraphJson.parseMap(raw);

            return WhatsAppSendResult.success(extractMessageId(response));
        } catch (RestClientException ex) {
            // Never leak the raw provider response (may contain account details) to callers/UI.
            log.warn("WhatsApp send failed for a recipient", ex);
            return WhatsAppSendResult.failure("WhatsApp delivery failed. " + failureHint);
        }
    }

    @SuppressWarnings("unchecked")
    private String extractMessageId(Map<String, Object> response) {
        try {
            var messages = (Iterable<Map<String, Object>>) response.get("messages");
            for (Map<String, Object> m : messages) {
                return String.valueOf(m.get("id"));
            }
        } catch (Exception ignored) {
            // fall through
        }
        return null;
    }
}
