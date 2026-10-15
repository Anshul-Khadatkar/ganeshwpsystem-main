package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.whatsapp.ImportMetaTemplateRequest;
import com.example.ganpativargani.dto.whatsapp.MessageTemplateDto;
import com.example.ganpativargani.dto.whatsapp.MessageTemplateRequest;
import com.example.ganpativargani.dto.whatsapp.MetaRemoteTemplateDto;
import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MessageTemplateType;
import com.example.ganpativargani.entity.MetaParameterFormat;
import com.example.ganpativargani.entity.MetaTemplateCategory;
import com.example.ganpativargani.entity.MetaTemplateStatus;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.MessageTemplateMapper;
import com.example.ganpativargani.repository.MessageTemplateRepository;
import com.example.ganpativargani.whatsapp.MetaTemplateBodyBuilder;
import com.example.ganpativargani.whatsapp.MetaTemplateClient;
import com.example.ganpativargani.whatsapp.MetaTemplateSyncResult;
import com.example.ganpativargani.whatsapp.MetaRemoteTemplate;
import com.example.ganpativargani.whatsapp.MetaTemplateValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MessageTemplateService {

    private final MessageTemplateRepository repository;
    private final MessageTemplateMapper mapper;
    private final AuditService auditService;
    private final MetaTemplateClient metaTemplateClient;

    public MessageTemplateService(MessageTemplateRepository repository, MessageTemplateMapper mapper,
                                   AuditService auditService, MetaTemplateClient metaTemplateClient) {
        this.repository = repository;
        this.mapper = mapper;
        this.auditService = auditService;
        this.metaTemplateClient = metaTemplateClient;
    }

    @Transactional(readOnly = true)
    public List<MessageTemplateDto> list(MessageTemplateType type) {
        List<MessageTemplate> templates = type != null
                ? repository.findByDeletedFalseAndTypeOrderByNameAsc(type)
                : repository.findByDeletedFalseOrderByNameAsc();
        return templates.stream().map(mapper::toDto).toList();
    }

    @Transactional(readOnly = true)
    public MessageTemplateDto getById(Long id) {
        return mapper.toDto(findEntity(id));
    }

    @Transactional
    public MessageTemplateDto create(MessageTemplateRequest request) {
        MessageTemplate template = MessageTemplate.builder()
                .name(request.getName().trim())
                .message(request.getMessage())
                .imageUrl(blankToNull(request.getImageUrl()))
                .type(request.getType())
                .active(request.isActive())
                .metaTemplateName(blankToNull(request.getMetaTemplateName()))
                .metaLanguageCode(request.getMetaLanguageCode())
                .metaParameterFormat(request.getMetaParameterFormat())
                .metaCategory(request.getMetaCategory() != null ? request.getMetaCategory() : MetaTemplateCategory.UTILITY)
                .build();
        template = repository.save(template);
        auditService.log("MessageTemplate", template.getId(), "CREATE", "Created template '" + template.getName() + "'");
        return mapper.toDto(template);
    }

    @Transactional
    public MessageTemplateDto update(Long id, MessageTemplateRequest request) {
        MessageTemplate template = findEntity(id);
        template.setName(request.getName().trim());
        template.setMessage(request.getMessage());
        template.setImageUrl(blankToNull(request.getImageUrl()));
        template.setType(request.getType());
        template.setActive(request.isActive());
        template.setMetaTemplateName(blankToNull(request.getMetaTemplateName()));
        template.setMetaLanguageCode(request.getMetaLanguageCode());
        template.setMetaParameterFormat(request.getMetaParameterFormat());
        if (request.getMetaCategory() != null) {
            template.setMetaCategory(request.getMetaCategory());
        }
        template = repository.save(template);
        auditService.log("MessageTemplate", template.getId(), "UPDATE", "Updated template '" + template.getName() + "'");
        return mapper.toDto(template);
    }

    @Transactional
    public MessageTemplateDto submitToMeta(Long id) {
        MessageTemplate template = findEntity(id);

        List<String> issues = MetaTemplateValidator.validateMessage(template.getMessage());
        if (!issues.isEmpty()) {
            throw new BadRequestException(String.join(" ", issues));
        }

        if (template.getMetaStatus() == MetaTemplateStatus.PENDING) {
            throw new BadRequestException("This template is already pending Meta review. Use 'Check status' to refresh.");
        }

        String baseName = template.getMetaTemplateName() != null && !template.getMetaTemplateName().isBlank()
                ? template.getMetaTemplateName().replaceAll("_v\\d+$", "")
                : MetaTemplateBodyBuilder.toMetaApiName(template.getName());

        // Meta cannot edit an approved template in place — editing / resubmitting always
        // creates a new Meta template name (…_v2, …_v3) and puts this row back to PENDING.
        if (template.getMetaStatus() == MetaTemplateStatus.APPROVED
                || template.getMetaStatus() == MetaTemplateStatus.REJECTED) {
            String current = template.getMetaTemplateName() != null && !template.getMetaTemplateName().isBlank()
                    ? template.getMetaTemplateName()
                    : baseName;
            template.setMetaTemplateName(MetaTemplateValidator.nextResubmitName(current));
            template.setMetaRejectionReason(null);
        } else if (template.getMetaTemplateName() == null || template.getMetaTemplateName().isBlank()) {
            boolean nameTakenOnMeta = metaTemplateClient.listFromMeta(true).stream()
                    .anyMatch(t -> t.name().equals(baseName));
            if (nameTakenOnMeta) {
                throw new BadRequestException(
                        "Meta already has an approved template named '" + baseName
                                + "'. Click Import from Meta, or use a different Meta template name.");
            }
            template.setMetaTemplateName(baseName);
        }

        MetaTemplateSyncResult result = metaTemplateClient.submit(template);
        applyMetaSync(template, result);
        template = repository.save(template);

        auditService.log("MessageTemplate", template.getId(), "META_SUBMIT",
                "Submitted template '" + template.getMetaTemplateName() + "' to Meta for review");
        return mapper.toDto(template);
    }

    @Transactional
    public MessageTemplateDto syncMetaStatus(Long id) {
        MessageTemplate template = findEntity(id);
        if (template.getMetaTemplateName() == null || template.getMetaTemplateName().isBlank()) {
            throw new BadRequestException("Submit this template to Meta first.");
        }

        MetaTemplateSyncResult result = metaTemplateClient.fetchStatus(
                template.getMetaTemplateName(), template.getMetaLanguageCode());
        applyMetaSync(template, result);
        template = repository.save(template);

        auditService.log("MessageTemplate", template.getId(), "META_SYNC",
                "Meta status for '" + template.getMetaTemplateName() + "': " + template.getMetaStatus());
        return mapper.toDto(template);
    }

    private void applyMetaSync(MessageTemplate template, MetaTemplateSyncResult result) {
        template.setMetaStatus(result.status());
        template.setMetaRejectionReason(result.rejectionReason());
        if (result.status() == MetaTemplateStatus.APPROVED) {
            template.setMetaRejectionReason(null);
        }
    }

    @Transactional(readOnly = true)
    public List<MetaRemoteTemplateDto> listApprovedFromMeta() {
        return metaTemplateClient.listFromMeta(true).stream()
                .map(remote -> MetaRemoteTemplateDto.builder()
                        .name(remote.name())
                        .language(remote.language())
                        .status(remote.status())
                        .category(remote.category())
                        .bodyPreview(remote.bodyText())
                        .rejectionReason(remote.rejectionReason())
                        .linkedLocally(repository.existsByDeletedFalseAndMetaTemplateNameAndMetaLanguageCode(
                                remote.name(), remote.language()))
                        .build())
                .toList();
    }

    @Transactional
    public MessageTemplateDto importFromMeta(ImportMetaTemplateRequest request) {
        MetaRemoteTemplate remote = metaTemplateClient
                .findRemote(request.getMetaTemplateName(), request.getMetaLanguageCode())
                .orElseThrow(() -> new BadRequestException("Template '" + request.getMetaTemplateName()
                        + "' (" + request.getMetaLanguageCode() + ") was not found in your Meta account."));
        if (remote.status() != MetaTemplateStatus.APPROVED) {
            throw new BadRequestException("Only Meta-approved templates can be imported. Current status: "
                    + remote.status());
        }

        String displayName = request.getName() != null && !request.getName().isBlank()
                ? request.getName().trim()
                : humanizeMetaName(remote.name());

        MessageTemplate template = repository
                .findByDeletedFalseAndMetaTemplateNameAndMetaLanguageCode(
                        remote.name(), remote.language())
                .orElseGet(() -> MessageTemplate.builder()
                        .type(request.getType())
                        .active(true)
                        .build());

        template.setName(displayName);
        template.setMessage(remote.bodyText() != null && !remote.bodyText().isBlank()
                ? remote.bodyText()
                : displayName);
        template.setType(request.getType());
        template.setMetaTemplateName(remote.name());
        template.setMetaLanguageCode(remote.language());
        template.setMetaCategory(remote.category());
        template.setMetaParameterFormat(detectParameterFormat(remote.bodyText()));
        template.setMetaStatus(MetaTemplateStatus.APPROVED);
        template.setMetaRejectionReason(null);
        template.setMetaHasImageHeader(remote.hasImageHeader());

        template = repository.save(template);
        auditService.log("MessageTemplate", template.getId(), "META_IMPORT",
                "Imported approved Meta template '" + remote.name() + "'");
        return mapper.toDto(template);
    }

    private MetaParameterFormat detectParameterFormat(String body) {
        if (body != null && body.matches(".*\\{\\{\\s*\\d+\\s*\\}\\}.*")) {
            return MetaParameterFormat.POSITIONAL;
        }
        return MetaParameterFormat.NAMED;
    }

    private String humanizeMetaName(String metaName) {
        String[] parts = metaName.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (part.isBlank()) {
                continue;
            }
            if (!sb.isEmpty()) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                sb.append(part.substring(1));
            }
        }
        return sb.isEmpty() ? metaName : sb.toString();
    }

    @Transactional
    public MessageTemplateDto setActive(Long id, boolean active) {
        MessageTemplate template = findEntity(id);
        template.setActive(active);
        template = repository.save(template);
        auditService.log("MessageTemplate", template.getId(), active ? "ACTIVATE" : "DEACTIVATE", null);
        return mapper.toDto(template);
    }

    /**
     * Soft-deletes locally. If the template was submitted to Meta, also deletes it there by name.
     */
    @Transactional
    public void delete(Long id) {
        MessageTemplate template = findEntity(id);
        String metaName = template.getMetaTemplateName();
        boolean submittedToMeta = metaName != null && !metaName.isBlank()
                && template.getMetaStatus() != null
                && template.getMetaStatus() != MetaTemplateStatus.NOT_SUBMITTED;

        if (submittedToMeta) {
            metaTemplateClient.deleteByName(metaName);
        }

        template.setActive(false);
        template.markDeleted();
        repository.save(template);
        auditService.log(
                "MessageTemplate",
                template.getId(),
                "DELETE",
                submittedToMeta
                        ? "Deleted template locally and from Meta ('" + metaName + "')"
                        : "Deleted template locally (not submitted to Meta)"
        );
    }

    /** Used by WhatsAppService when a donation-thank-you send doesn't specify a template explicitly. */
    @Transactional(readOnly = true)
    public MessageTemplate getDefaultDonationThankYouTemplate() {
        List<MessageTemplate> active = repository
                .findByDeletedFalseAndActiveTrueAndTypeOrderByIdAsc(MessageTemplateType.DONATION_THANK_YOU);
        return active.stream()
                .filter(MessageTemplate::isMetaApproved)
                .findFirst()
                .or(() -> active.stream().findFirst())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No active donation thank-you template configured. Add one under WhatsApp > Templates."));
    }

    MessageTemplate findEntity(Long id) {
        return repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message template not found: " + id));
    }

    private String blankToNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }
}
