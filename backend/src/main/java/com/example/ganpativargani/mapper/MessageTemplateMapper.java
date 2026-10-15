package com.example.ganpativargani.mapper;

import com.example.ganpativargani.dto.whatsapp.MessageTemplateDto;
import com.example.ganpativargani.entity.MessageTemplate;
import org.springframework.stereotype.Component;

@Component
public class MessageTemplateMapper {

    public MessageTemplateDto toDto(MessageTemplate template) {
        return MessageTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .message(template.getMessage())
                .imageUrl(template.getImageUrl())
                .type(template.getType())
                .active(template.isActive())
                .metaTemplateName(template.getMetaTemplateName())
                .metaLanguageCode(template.getMetaLanguageCode())
                .metaParameterFormat(template.getMetaParameterFormat())
                .metaStatus(template.getMetaStatus())
                .metaRejectionReason(template.getMetaRejectionReason())
                .metaCategory(template.getMetaCategory())
                .metaHasImageHeader(template.isMetaHasImageHeader())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .build();
    }
}
