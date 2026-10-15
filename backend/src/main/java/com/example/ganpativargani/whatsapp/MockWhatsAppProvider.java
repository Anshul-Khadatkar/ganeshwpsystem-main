package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaParameterFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Default provider. Safe to run with no WhatsApp credentials configured at
 * all - useful for local development and for this app before real Meta
 * credentials are wired in. Activated when app.whatsapp.provider=mock
 * (the default - see application.yml) or is unset.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockWhatsAppProvider implements WhatsAppProvider {

    @Override
    public WhatsAppSendResult send(WhatsAppMessageRequest request) {
        String fakeId = "mock-" + UUID.randomUUID();
        log.info("[MOCK WHATSAPP] to={} image={} message=\n{}",
                request.toPhoneNumber(), request.imageUrl() != null ? request.imageUrl() : "(none)", request.message());
        return WhatsAppSendResult.success(fakeId);
    }

    @Override
    public WhatsAppSendResult sendTemplate(String toPhoneNumber, String templateName, String languageCode,
                                            MetaParameterFormat parameterFormat, Map<String, String> namedParameters,
                                            String headerImageUrl) {
        String fakeId = "mock-template-" + UUID.randomUUID();
        log.info("[MOCK WHATSAPP TEMPLATE] to={} template={} lang={} format={} headerImage={} params={}",
                toPhoneNumber, templateName, languageCode, parameterFormat,
                headerImageUrl != null ? headerImageUrl : "(none)", namedParameters);
        return WhatsAppSendResult.success(fakeId);
    }
}

