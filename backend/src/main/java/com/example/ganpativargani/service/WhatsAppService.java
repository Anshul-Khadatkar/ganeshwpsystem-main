package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.DonationDto;
import com.example.ganpativargani.dto.whatsapp.DonationSendRequest;
import com.example.ganpativargani.entity.Donation;
import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MessageTemplateType;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.DonationMapper;
import com.example.ganpativargani.repository.DonationRepository;
import com.example.ganpativargani.whatsapp.DonationMessageVariables;
import com.example.ganpativargani.whatsapp.MetaTemplateBodyBuilder;
import com.example.ganpativargani.whatsapp.TemplateVariableResolver;
import com.example.ganpativargani.whatsapp.WhatsAppMessageRequest;
import com.example.ganpativargani.whatsapp.WhatsAppProvider;
import com.example.ganpativargani.whatsapp.WhatsAppSendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Handles WhatsApp sends that are tied to a single {@link Donation} (the
 * thank-you flow). This is deliberately a thin layer over
 * {@link WhatsAppProvider} + {@link TemplateVariableResolver} - the exact
 * same building blocks {@link BroadcastService} uses for announcements -
 * so donation messaging is NOT a special case baked into the provider.
 */
@Service
public class WhatsAppService {

    private final DonationRepository donationRepository;
    private final DonationMapper donationMapper;
    private final MessageTemplateService templateService;
    private final AppSettingsService appSettingsService;
    private final WhatsAppProvider provider;
    private final AuditService auditService;

    public WhatsAppService(DonationRepository donationRepository, DonationMapper donationMapper,
                            MessageTemplateService templateService, AppSettingsService appSettingsService,
                            WhatsAppProvider provider, AuditService auditService) {
        this.donationRepository = donationRepository;
        this.donationMapper = donationMapper;
        this.templateService = templateService;
        this.appSettingsService = appSettingsService;
        this.provider = provider;
        this.auditService = auditService;
    }

    @Transactional
    public DonationDto sendDonationThankYou(Long donationId, DonationSendRequest request) {
        Donation donation = findDonation(donationId);

        MessageTemplate template = (request != null && request.getTemplateId() != null)
                ? templateService.findEntity(request.getTemplateId())
                : templateService.getDefaultDonationThankYouTemplate();

        if (template.getType() != MessageTemplateType.DONATION_THANK_YOU) {
            throw new BadRequestException(
                    "Template '" + template.getName() + "' is not a Donation thank you template.");
        }
        if (!template.isActive()) {
            throw new BadRequestException(
                    "Template '" + template.getName() + "' is inactive. Activate it under WhatsApp > Templates.");
        }

        donation.setWhatsappStatus(WhatsAppStatus.PENDING);
        donationRepository.save(donation);

        var variables = DonationMessageVariables.build(donation, appSettingsService.getOrganizationName());

        WhatsAppSendResult result;
        String auditNote;

        if (template.isMetaApproved()) {
            // Business-initiated send via a Meta-approved template - works regardless of
            // whether the contributor has messaged the business number recently.
            String headerImage = null;
            if (template.isMetaHasImageHeader()) {
                headerImage = (request != null && request.getImageUrlOverride() != null
                        && !request.getImageUrlOverride().isBlank())
                        ? request.getImageUrlOverride()
                        : template.getImageUrl();
                if (headerImage == null || headerImage.isBlank()) {
                    throw new BadRequestException(
                            "Template '" + template.getName() + "' requires an image URL (Meta IMAGE header).");
                }
            }
            result = provider.sendTemplate(
                    donation.getContributor().getMobileNumber(),
                    template.getMetaTemplateName(),
                    template.getMetaLanguageCode(),
                    template.getMetaParameterFormat(),
                    MetaTemplateBodyBuilder.parametersForSend(template.getMessage(), variables),
                    headerImage
            );
            auditNote = "Thank-you message via Meta-approved template '" + template.getMetaTemplateName() + "'";
        } else {
            // Free-form fallback - only deliverable inside the 24-hour messaging window.
            String imageUrl = (request != null && request.getImageUrlOverride() != null && !request.getImageUrlOverride().isBlank())
                    ? request.getImageUrlOverride()
                    : template.getImageUrl();
            String resolvedMessage = TemplateVariableResolver.resolve(template.getMessage(), variables);

            result = provider.send(new WhatsAppMessageRequest(
                    donation.getContributor().getMobileNumber(), resolvedMessage, imageUrl));
            auditNote = "Thank-you message via template '" + template.getName() + "' (free-form)";
        }

        applyResult(donation, result);
        donationRepository.save(donation);

        auditService.log("Donation", donation.getId(),
                result.success() ? "WHATSAPP_SEND" : "WHATSAPP_SEND_FAILED", auditNote);

        return donationMapper.toDto(donation);
    }

    @Transactional
    public DonationDto retryDonationSend(Long donationId) {
        // Deliberately reuses the exact same path as the original send - no separate
        // "retry" logic to duplicate/drift, and no new donation or duplicate record is created.
        return sendDonationThankYou(donationId, null);
    }

    private void applyResult(Donation donation, WhatsAppSendResult result) {
        if (result.success()) {
            donation.setWhatsappStatus(WhatsAppStatus.SENT);
            donation.setWhatsappMessageId(result.providerMessageId());
            donation.setWhatsappSentAt(LocalDateTime.now());
            donation.setWhatsappErrorMessage(null);
        } else {
            donation.setWhatsappStatus(WhatsAppStatus.FAILED);
            donation.setWhatsappErrorMessage(result.errorMessage());
        }
    }

    private Donation findDonation(Long id) {
        return donationRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donation not found: " + id));
    }
}
