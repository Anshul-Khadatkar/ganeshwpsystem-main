package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.MessageTemplateType;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImportMetaTemplateRequest {

    @NotBlank
    private String metaTemplateName;

    @NotBlank
    private String metaLanguageCode;

    /** Display name in this app. Defaults to a title-cased version of the Meta name. */
    private String name;

    private MessageTemplateType type = MessageTemplateType.CUSTOM;
}
