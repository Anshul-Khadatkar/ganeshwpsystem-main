package com.example.ganpativargani.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * A reusable, admin-editable WhatsApp message. Supports {{variable}}
 * placeholders (resolved by {@link com.example.ganpativargani.whatsapp.TemplateVariableResolver})
 * and an optional image - both changeable from the UI with no redeploy.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
@ToString
@Entity
@Table(name = "message_templates")
public class MessageTemplate extends BaseEntity {

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Column(name = "image_url", length = 1000)
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private MessageTemplateType type;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private boolean active = true;

    /**
     * The exact name Meta assigned this template once approved (e.g.
     * "donation_thank_you") - NOT the same as {@link #name} above, which is
     * just this app's own display label. Null until an admin fills it in
     * after approval; while null, sends fall back to free-form text/image
     * (which only reaches contributors inside the 24-hour messaging window).
     */
    @Column(name = "meta_template_name", length = 200)
    private String metaTemplateName;

    @Column(name = "meta_language_code", length = 10)
    @Builder.Default
    private String metaLanguageCode = "en_US";

    /** Whether the approved template uses named ({{name}}) or positional ({{1}}) parameters. */
    @Enumerated(EnumType.STRING)
    @Column(name = "meta_parameter_format", length = 20)
    @Builder.Default
    private MetaParameterFormat metaParameterFormat = MetaParameterFormat.NAMED;

    /** Tracks submission to Meta via the app's Submit to Meta action. */
    @Enumerated(EnumType.STRING)
    @Column(name = "meta_status", length = 20)
    @Builder.Default
    private MetaTemplateStatus metaStatus = MetaTemplateStatus.NOT_SUBMITTED;

    @Column(name = "meta_rejection_reason", columnDefinition = "TEXT")
    private String metaRejectionReason;

    @Enumerated(EnumType.STRING)
    @Column(name = "meta_category", length = 20)
    @Builder.Default
    private MetaTemplateCategory metaCategory = MetaTemplateCategory.UTILITY;

    /**
     * True when the Meta-approved template was created with an IMAGE header.
     * Send path then includes a header image parameter using {@link #imageUrl}.
     */
    @Column(name = "meta_has_image_header", nullable = false)
    @Builder.Default
    private boolean metaHasImageHeader = false;

    public boolean isMetaApproved() {
        return metaStatus == MetaTemplateStatus.APPROVED
                && metaTemplateName != null
                && !metaTemplateName.isBlank();
    }
}
