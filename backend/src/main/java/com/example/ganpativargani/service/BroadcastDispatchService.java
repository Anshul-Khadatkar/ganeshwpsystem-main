package com.example.ganpativargani.service;

import com.example.ganpativargani.entity.Broadcast;
import com.example.ganpativargani.entity.BroadcastRecipient;
import com.example.ganpativargani.entity.BroadcastStatus;
import com.example.ganpativargani.entity.Contributor;
import com.example.ganpativargani.entity.MessageTemplate;
import com.example.ganpativargani.entity.MetaParameterFormat;
import com.example.ganpativargani.entity.WhatsAppStatus;
import com.example.ganpativargani.repository.BroadcastRecipientRepository;
import com.example.ganpativargani.repository.BroadcastRepository;
import com.example.ganpativargani.whatsapp.ContributorMessageVariables;
import com.example.ganpativargani.whatsapp.MetaTemplateBodyBuilder;
import com.example.ganpativargani.whatsapp.TemplateVariableResolver;
import com.example.ganpativargani.whatsapp.WhatsAppMessageRequest;
import com.example.ganpativargani.whatsapp.WhatsAppProvider;
import com.example.ganpativargani.whatsapp.WhatsAppSendResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Runs WhatsApp broadcast sends off the HTTP request thread so Cloudflare/nginx
 * do not cut off large blasts mid-way (which looked like CORS / "something went wrong").
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BroadcastDispatchService {

    private static final long PACE_MILLIS = 50L;

    private final BroadcastRepository broadcastRepository;
    private final BroadcastRecipientRepository recipientRepository;
    private final AppSettingsService appSettingsService;
    private final WhatsAppProvider provider;
    private final AuditService auditService;
    private final PlatformTransactionManager transactionManager;

    @Async
    public void dispatchPendingAsync(Long broadcastId) {
        try {
            runDispatch(broadcastId, false);
        } catch (Exception ex) {
            log.error("Async broadcast {} failed", broadcastId, ex);
            markTerminalOnError(broadcastId, ex.getMessage());
        }
    }

    @Async
    public void retryFailedAsync(Long broadcastId) {
        try {
            runDispatch(broadcastId, true);
        } catch (Exception ex) {
            log.error("Async broadcast retry {} failed", broadcastId, ex);
            markTerminalOnError(broadcastId, ex.getMessage());
        }
    }

    private void runDispatch(Long broadcastId, boolean failedOnly) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);

        List<Long> recipientIds = tx.execute(status -> {
            Broadcast broadcast = broadcastRepository.findByIdAndDeletedFalse(broadcastId).orElse(null);
            if (broadcast == null) {
                log.warn("Broadcast {} missing; skipping dispatch", broadcastId);
                return List.of();
            }
            broadcast.setStatus(BroadcastStatus.IN_PROGRESS);
            broadcastRepository.save(broadcast);

            List<BroadcastRecipient> recipients = failedOnly
                    ? recipientRepository.findByBroadcast_IdAndStatus(broadcastId, WhatsAppStatus.FAILED)
                    : recipientRepository.findByBroadcast_IdAndStatus(broadcastId, WhatsAppStatus.PENDING);

            if (recipients.isEmpty() && !failedOnly) {
                recipients = recipientRepository.findByBroadcast_IdOrderByIdAsc(broadcastId).stream()
                        .filter(r -> r.getStatus() == WhatsAppStatus.PENDING || r.getStatus() == WhatsAppStatus.FAILED)
                        .toList();
            }

            List<Long> ids = new ArrayList<>(recipients.size());
            for (BroadcastRecipient r : recipients) {
                if (failedOnly) {
                    r.setStatus(WhatsAppStatus.PENDING);
                    r.setErrorMessage(null);
                    recipientRepository.save(r);
                }
                ids.add(r.getId());
            }
            log.info("Dispatching broadcast {} to {} recipient(s) (failedOnly={})",
                    broadcastId, ids.size(), failedOnly);
            return ids;
        });

        if (recipientIds == null || recipientIds.isEmpty()) {
            tx.executeWithoutResult(status -> refreshCounts(broadcastId));
            return;
        }

        String organizationName = appSettingsService.getOrganizationName();

        for (int i = 0; i < recipientIds.size(); i++) {
            if (i > 0 && PACE_MILLIS > 0) {
                try {
                    Thread.sleep(PACE_MILLIS);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    log.warn("Broadcast {} interrupted after {} sends", broadcastId, i);
                    break;
                }
            }

            Long recipientId = recipientIds.get(i);
            SendJob job = tx.execute(status -> loadJob(recipientId, organizationName));
            if (job == null) {
                continue;
            }

            WhatsAppSendResult result = send(job);

            final int index = i;
            tx.executeWithoutResult(status -> {
                BroadcastRecipient recipient = recipientRepository.findById(recipientId).orElse(null);
                if (recipient == null) {
                    return;
                }
                if (result.success()) {
                    recipient.setStatus(WhatsAppStatus.SENT);
                    recipient.setProviderMessageId(result.providerMessageId());
                    recipient.setErrorMessage(null);
                    recipient.setSentAt(LocalDateTime.now());
                } else {
                    recipient.setStatus(WhatsAppStatus.FAILED);
                    recipient.setErrorMessage(result.errorMessage());
                }
                recipientRepository.save(recipient);

                if (index == 0 || index == recipientIds.size() - 1 || (index + 1) % 10 == 0) {
                    refreshCounts(broadcastId);
                }
            });
        }

        tx.executeWithoutResult(status -> {
            refreshCounts(broadcastId);
            Broadcast broadcast = broadcastRepository.findByIdAndDeletedFalse(broadcastId).orElse(null);
            if (broadcast == null) {
                return;
            }
            auditService.log(
                    "Broadcast",
                    broadcast.getId(),
                    failedOnly ? "BROADCAST_RETRY_DONE" : "BROADCAST_SEND_DONE",
                    "Finished: sent=" + broadcast.getSuccessfulCount() + " failed=" + broadcast.getFailedCount()
            );
            log.info("Broadcast {} finished: sent={} failed={}",
                    broadcastId, broadcast.getSuccessfulCount(), broadcast.getFailedCount());
        });
    }

    private SendJob loadJob(Long recipientId, String organizationName) {
        BroadcastRecipient recipient = recipientRepository.findById(recipientId).orElse(null);
        if (recipient == null) {
            return null;
        }
        Contributor contributor = recipient.getContributor();
        Broadcast broadcast = recipient.getBroadcast();
        MessageTemplate template = broadcast.getTemplate();
        Map<String, String> variables = ContributorMessageVariables.build(contributor, organizationName);

        String templateMessage = null;
        String metaTemplateName = null;
        String metaLanguageCode = null;
        MetaParameterFormat metaParameterFormat = null;
        boolean metaApproved = false;
        boolean metaHasImageHeader = false;
        String templateImageUrl = null;

        if (template != null) {
            templateMessage = template.getMessage();
            metaTemplateName = template.getMetaTemplateName();
            metaLanguageCode = template.getMetaLanguageCode();
            metaParameterFormat = template.getMetaParameterFormat();
            metaApproved = template.isMetaApproved();
            metaHasImageHeader = template.isMetaHasImageHeader();
            templateImageUrl = template.getImageUrl();
        }

        return new SendJob(
                recipient.getPhoneNumber(),
                broadcast.getMessage(),
                broadcast.getImageUrl(),
                metaApproved,
                metaHasImageHeader,
                metaTemplateName,
                metaLanguageCode,
                metaParameterFormat,
                templateMessage,
                templateImageUrl,
                variables
        );
    }

    private WhatsAppSendResult send(SendJob job) {
        if (job.metaApproved()) {
            if (job.metaHasImageHeader()
                    && (job.templateImageUrl() == null || job.templateImageUrl().isBlank())
                    && (job.broadcastImageUrl() == null || job.broadcastImageUrl().isBlank())) {
                return WhatsAppSendResult.failure(
                        "Template requires an image URL (Meta IMAGE header). Set Image URL on the template.");
            }
            String headerImage = job.metaHasImageHeader()
                    ? (job.broadcastImageUrl() != null && !job.broadcastImageUrl().isBlank()
                    ? job.broadcastImageUrl() : job.templateImageUrl())
                    : null;
            return provider.sendTemplate(
                    job.phoneNumber(),
                    job.metaTemplateName(),
                    job.metaLanguageCode(),
                    job.metaParameterFormat(),
                    MetaTemplateBodyBuilder.parametersForSend(job.templateMessage(), job.variables()),
                    headerImage
            );
        }

        String resolvedMessage = TemplateVariableResolver.resolve(job.broadcastMessage(), job.variables());
        return provider.send(new WhatsAppMessageRequest(job.phoneNumber(), resolvedMessage, job.broadcastImageUrl()));
    }

    private void refreshCounts(Long broadcastId) {
        Broadcast broadcast = broadcastRepository.findByIdAndDeletedFalse(broadcastId).orElse(null);
        if (broadcast == null) {
            return;
        }
        List<BroadcastRecipient> all = recipientRepository.findByBroadcast_IdOrderByIdAsc(broadcastId);
        long sent = all.stream().filter(r -> r.getStatus() == WhatsAppStatus.SENT).count();
        long failed = all.stream().filter(r -> r.getStatus() == WhatsAppStatus.FAILED).count();
        long pending = all.stream().filter(r -> r.getStatus() == WhatsAppStatus.PENDING).count();

        broadcast.setSuccessfulCount((int) sent);
        broadcast.setFailedCount((int) failed);
        if (pending > 0) {
            broadcast.setStatus(BroadcastStatus.IN_PROGRESS);
        } else if (failed == 0) {
            broadcast.setStatus(BroadcastStatus.COMPLETED);
        } else if (sent == 0) {
            broadcast.setStatus(BroadcastStatus.FAILED);
        } else {
            broadcast.setStatus(BroadcastStatus.COMPLETED_WITH_ERRORS);
        }
        broadcastRepository.save(broadcast);
    }

    private void markTerminalOnError(Long broadcastId, String error) {
        TransactionTemplate tx = new TransactionTemplate(transactionManager);
        tx.executeWithoutResult(status -> {
            refreshCounts(broadcastId);
            broadcastRepository.findByIdAndDeletedFalse(broadcastId).ifPresent(broadcast -> {
                if (broadcast.getStatus() == BroadcastStatus.IN_PROGRESS) {
                    broadcast.setStatus(BroadcastStatus.COMPLETED_WITH_ERRORS);
                    broadcastRepository.save(broadcast);
                }
                auditService.log("Broadcast", broadcastId, "BROADCAST_DISPATCH_ERROR", error);
            });
        });
    }

    private record SendJob(
            String phoneNumber,
            String broadcastMessage,
            String broadcastImageUrl,
            boolean metaApproved,
            boolean metaHasImageHeader,
            String metaTemplateName,
            String metaLanguageCode,
            MetaParameterFormat metaParameterFormat,
            String templateMessage,
            String templateImageUrl,
            Map<String, String> variables
    ) {
    }
}
