package com.example.ganpativargani.whatsapp;

import com.example.ganpativargani.entity.MetaTemplateStatus;

public record MetaTemplateSyncResult(MetaTemplateStatus status, String rejectionReason) {
}
