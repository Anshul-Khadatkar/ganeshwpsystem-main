package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.MessageTemplateType;
import com.example.ganpativargani.entity.MetaParameterFormat;
import com.example.ganpativargani.entity.MetaTemplateCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MessageTemplateRequest {

    @NotBlank(message = "Template name is required")
    private String name;

    @NotBlank(message = "Message text is required")
    private String message;

    /** Always optional - a template can be text-only. */
    private String imageUrl;

    @NotNull(message = "Template type is required")
    private MessageTemplateType type;

    private boolean active = true;

    /**
     * Optional. Fill this in once the corresponding template is approved in
     * Meta Business Manager, to enable business-initiated sends outside the
     * 24-hour messaging window. Must match Meta's registered name exactly.
     */
    private String metaTemplateName;

    private String metaLanguageCode = "en_US";

    private MetaParameterFormat metaParameterFormat = MetaParameterFormat.NAMED;

    /** Meta category used when submitting via the app's Submit to Meta action. */
    private MetaTemplateCategory metaCategory = MetaTemplateCategory.UTILITY;
}
