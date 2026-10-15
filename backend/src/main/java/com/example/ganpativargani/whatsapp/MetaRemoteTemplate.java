package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaTemplateCategory;
import com.example.ganpativargani.entity.MetaTemplateStatus;

public record MetaRemoteTemplate(
        String name,
        String language,
        MetaTemplateStatus status,
        MetaTemplateCategory category,
        String bodyText,
        String rejectionReason,
        boolean hasImageHeader
) {
}
