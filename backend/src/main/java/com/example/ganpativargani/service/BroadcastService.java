package com.example.ganpativargani.service;

import com.example.ganpativargani.dto.PageResponse;
import com.example.ganpativargani.dto.whatsapp.BroadcastCreateRequest;
import com.example.ganpativargani.dto.whatsapp.BroadcastCreateRequest.RecipientMode;
import com.example.ganpativargani.dto.whatsapp.BroadcastDetailDto;
import com.example.ganpativargani.dto.whatsapp.BroadcastDto;
import com.example.ganpativargani.dto.whatsapp.BroadcastPreviewResponse;
import com.example.ganpativargani.entity.Broadcast;
import com.example.ganpativargani.entity.BroadcastRecipient;
import com.example.ganpativargani.entity.BroadcastStatus;
import com.example.ganpativargani.entity.Building;
import com.example.ganpativargani.entity.Contributor;
import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.exception.BadRequestException;
import com.example.ganpativargani.exception.ResourceNotFoundException;
import com.example.ganpativargani.mapper.BroadcastMapper;
import com.example.ganpativargani.repository.BroadcastRecipientRepository;
import com.example.ganpativargani.repository.BroadcastRepository;
import com.example.ganpativargani.repository.BuildingRepository;
import com.example.ganpativargani.repository.ContributorRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Objects;

/**
 * Owns the full broadcast lifecycle: resolving recipients, creating tracked rows,
 * then handing off WhatsApp delivery to {@link BroadcastDispatchService} so the
 * HTTP request returns before Cloudflare/nginx time out on large blasts.
 */
@Service
public class BroadcastService {

    private final BroadcastRepository broadcastRepository;
    private final BroadcastRecipientRepository recipientRepository;
    private final ContributorRepository contributorRepository;
    private final BuildingRepository buildingRepository;
    private final MessageTemplateService templateService;
    private final BroadcastDispatchService dispatchService;
    private final BroadcastMapper mapper;
    private final AuditService auditService;

    public BroadcastService(BroadcastRepository broadcastRepository, BroadcastRecipientRepository recipientRepository,
                             ContributorRepository contributorRepository, BuildingRepository buildingRepository,
                             MessageTemplateService templateService, BroadcastDispatchService dispatchService,
                             BroadcastMapper mapper, AuditService auditService) {
        this.broadcastRepository = broadcastRepository;
        this.recipientRepository = recipientRepository;
        this.contributorRepository = contributorRepository;
        this.buildingRepository = buildingRepository;
        this.templateService = templateService;
        this.dispatchService = dispatchService;
        this.mapper = mapper;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public BroadcastPreviewResponse preview(BroadcastCreateRequest request) {
        List<Contributor> recipients = resolveRecipients(request);
        return BroadcastPreviewResponse.builder()
                .recipientCount(recipients.size())
                .recipientCriteria(describeCriteria(request))
                .sampleNames(recipients.stream().limit(5).map(Contributor::getName).toList())
                .build();
    }

    @Transactional
    public BroadcastDto create(BroadcastCreateRequest request) {
        List<Contributor> recipients = resolveRecipients(request);
        if (recipients.isEmpty()) {
            throw new BadRequestException("No opted-in contributors match these recipient criteria.");
        }

        MessageTemplate template = request.getTemplateId() != null
                ? templateService.findEntity(request.getTemplateId())
                : null;

        Broadcast broadcast = Broadcast.builder()
                .template(template)
                .message(request.getMessage())
                .imageUrl(request.getImageUrl())
                .createdBy(currentUser())
                .recipientCriteria(describeCriteria(request))
                .status(BroadcastStatus.IN_PROGRESS)
                .totalRecipients(recipients.size())
                .successfulCount(0)
                .failedCount(0)
                .build();
        broadcast = broadcastRepository.save(broadcast);

        Broadcast savedBroadcast = broadcast;
        List<BroadcastRecipient> recipientRows = recipients.stream()
                .map(c -> BroadcastRecipient.builder()
                        .broadcast(savedBroadcast)
                        .contributor(c)
                        .phoneNumber(c.getMobileNumber())
                        .status(WhatsAppStatus.PENDING)
                        .build())
                .toList();
        recipientRepository.saveAll(recipientRows);

        Long broadcastId = broadcast.getId();
        queueAfterCommit(() -> dispatchService.dispatchPendingAsync(broadcastId));

        auditService.log("Broadcast", broadcast.getId(), "BROADCAST_QUEUED",
                "Queued send to " + broadcast.getTotalRecipients() + " recipients (" + broadcast.getRecipientCriteria() + ")");

        return mapper.toDto(broadcast);
    }

    @Transactional
    public BroadcastDto retryFailed(Long broadcastId) {
        Broadcast broadcast = findBroadcast(broadcastId);
        List<BroadcastRecipient> failed = recipientRepository.findByBroadcast_IdAndStatus(broadcastId, WhatsAppStatus.FAILED);
        if (!failed.isEmpty()) {
            broadcast.setStatus(BroadcastStatus.IN_PROGRESS);
            broadcastRepository.save(broadcast);
            queueAfterCommit(() -> dispatchService.retryFailedAsync(broadcastId));
            auditService.log("Broadcast", broadcast.getId(), "BROADCAST_RETRY_QUEUED",
                    "Queued retry for " + failed.size() + " failed recipients");
        }
        return mapper.toDto(broadcast);
    }

    @Transactional(readOnly = true)
    public PageResponse<BroadcastDto> history(int page, int size) {
        var result = broadcastRepository.findByDeletedFalseOrderByCreatedAtDesc(PageRequest.of(page, size));
        return PageResponse.from(result.map(mapper::toDto));
    }

    @Transactional(readOnly = true)
    public BroadcastDetailDto getDetail(Long id, int page, int size) {
        Broadcast broadcast = findBroadcast(id);
        var recipientPage = recipientRepository
                .findByBroadcast_IdOrderByIdAsc(id, PageRequest.of(page, size))
                .map(mapper::toDto);
        return BroadcastDetailDto.builder()
                .broadcast(mapper.toDto(broadcast))
                .recipients(PageResponse.from(recipientPage))
                .build();
    }

    private void queueAfterCommit(Runnable task) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    task.run();
                }
            });
        } else {
            task.run();
        }
    }

    private List<Contributor> resolveRecipients(BroadcastCreateRequest request) {
        RecipientMode mode = request.getRecipientMode();

        if (mode == RecipientMode.EXPLICIT) {
            if (request.getContributorIds() == null || request.getContributorIds().isEmpty()) {
                throw new BadRequestException("At least one recipient is required");
            }
            return request.getContributorIds().stream()
                    .map(id -> contributorRepository.findByIdAndDeletedFalse(id)
                            .filter(Contributor::isWhatsappOptIn)
                            .orElse(null))
                    .filter(Objects::nonNull)
                    .toList();
        }

        Long buildingId = mode == RecipientMode.FILTERED ? request.getBuildingId() : null;
        var minAmount = mode == RecipientMode.FILTERED ? request.getMinDonationAmount() : null;
        var dateFrom = mode == RecipientMode.FILTERED ? request.getDonatedFrom() : null;
        var dateTo = mode == RecipientMode.FILTERED ? request.getDonatedTo() : null;

        return contributorRepository.findBroadcastRecipients(buildingId, minAmount, dateFrom, dateTo);
    }

    private String describeCriteria(BroadcastCreateRequest request) {
        if (request.getRecipientMode() == RecipientMode.EXPLICIT) {
            int count = request.getContributorIds() == null ? 0 : request.getContributorIds().size();
            return count + " manually selected contributor(s)";
        }
        if (request.getRecipientMode() == RecipientMode.ALL) {
            return "All opted-in contributors";
        }

        StringBuilder sb = new StringBuilder();
        if (request.getBuildingId() != null) {
            Building building = buildingRepository.findByIdAndDeletedFalse(request.getBuildingId())
                    .orElseThrow(() -> new ResourceNotFoundException("Building not found: " + request.getBuildingId()));
            sb.append(building.getName());
        } else {
            sb.append("All buildings");
        }
        if (request.getMinDonationAmount() != null) {
            sb.append(", donated \u2265 \u20b9").append(request.getMinDonationAmount());
        }
        if (request.getDonatedFrom() != null || request.getDonatedTo() != null) {
            sb.append(", between ").append(request.getDonatedFrom()).append(" and ").append(request.getDonatedTo());
        }
        return sb.toString();
    }

    private Broadcast findBroadcast(Long id) {
        return broadcastRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Broadcast not found: " + id));
    }

    private String currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal()))
                ? auth.getName()
                : "system";
    }
}
