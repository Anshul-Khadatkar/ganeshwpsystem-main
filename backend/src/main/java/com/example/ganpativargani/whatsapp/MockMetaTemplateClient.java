package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MetaTemplateStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "app.whatsapp", name = "provider", havingValue = "mock", matchIfMissing = true)
public class MockMetaTemplateClient implements MetaTemplateClient {

    @Override
    public MetaTemplateSyncResult submit(MessageTemplate template) {
        String apiName = template.getMetaTemplateName() != null && !template.getMetaTemplateName().isBlank()
                ? template.getMetaTemplateName()
                : MetaTemplateBodyBuilder.toMetaApiName(template.getName());
        log.info("[MOCK META TEMPLATE] Submitted '{}' for review (auto-approves on sync)", apiName);
        if (template.getImageUrl() != null && !template.getImageUrl().isBlank()) {
            template.setMetaHasImageHeader(true);
        }
        return new MetaTemplateSyncResult(MetaTemplateStatus.PENDING, null);
    }

    @Override
    public MetaTemplateSyncResult fetchStatus(String templateName, String languageCode) {
        log.info("[MOCK META TEMPLATE] Status check for '{}' ({}) -> APPROVED", templateName, languageCode);
        return new MetaTemplateSyncResult(MetaTemplateStatus.APPROVED, null);
    }

    @Override
    public List<MetaRemoteTemplate> listFromMeta(boolean approvedOnly) {
        var sample = new MetaRemoteTemplate(
                "donation_thank_you",
                "en_US",
                MetaTemplateStatus.APPROVED,
                com.example.ganpativargani.entity.MetaTemplateCategory.UTILITY,
                "Thank you {{name}} for your generous contribution of Rs.{{amount}}.",
                null,
                false
        );
        if (approvedOnly) {
            return List.of(sample);
        }
        return List.of(
                sample,
                new MetaRemoteTemplate(
                        "festival_announcement",
                        "en_US",
                        MetaTemplateStatus.PENDING,
                        com.example.ganpativargani.entity.MetaTemplateCategory.MARKETING,
                        "Join us for the festival celebration tomorrow!",
                        null,
                        false
                )
        );
    }

    @Override
    public Optional<MetaRemoteTemplate> findRemote(String templateName, String languageCode) {
        return listFromMeta(false).stream()
                .filter(t -> t.name().equals(templateName) && t.language().equals(languageCode))
                .findFirst();
    }

    @Override
    public void deleteByName(String templateName) {
        log.info("[MOCK META TEMPLATE] Deleted template '{}'", templateName);
    }
}
