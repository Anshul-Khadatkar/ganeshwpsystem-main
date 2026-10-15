package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.MetaTemplateCategory;
import com.example.ganpativargani.entity.MetaTemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MetaRemoteTemplateDto {
    private String name;
    private String language;
    private MetaTemplateStatus status;
    private MetaTemplateCategory category;
    private String bodyPreview;
    private String rejectionReason;
    /** True if a local app template is already linked to this Meta name + language. */
    private boolean linkedLocally;
}
