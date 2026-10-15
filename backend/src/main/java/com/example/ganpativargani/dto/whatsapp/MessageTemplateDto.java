package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.MessageTemplateType;
import com.example.ganpativargani.entity.MetaParameterFormat;
import com.example.ganpativargani.entity.MetaTemplateCategory;
import com.example.ganpativargani.entity.MetaTemplateStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class MessageTemplateDto {
    private Long id;
    private String name;
    private String message;
    private String imageUrl;
    private MessageTemplateType type;
    private boolean active;

    /** Null until an admin links this to a Meta-approved template after approval. */
    private String metaTemplateName;
    private String metaLanguageCode;
    private MetaParameterFormat metaParameterFormat;

    private MetaTemplateStatus metaStatus;
    private String metaRejectionReason;
    private MetaTemplateCategory metaCategory;
    /** True when Meta template was approved with an IMAGE header. */
    private boolean metaHasImageHeader;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
