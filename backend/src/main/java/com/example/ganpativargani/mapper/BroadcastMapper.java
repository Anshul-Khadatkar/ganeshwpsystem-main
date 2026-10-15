package com.example.ganpativargani.mapper;

import com.example.ganpativargani.dto.whatsapp.BroadcastDto;
import com.example.ganpativargani.dto.whatsapp.BroadcastRecipientDto;
import com.example.ganpativargani.entity.Broadcast;
import com.example.ganpativargani.entity.BroadcastRecipient;
import org.springframework.stereotype.Component;

@Component
public class BroadcastMapper {

    public BroadcastDto toDto(Broadcast broadcast) {
        return BroadcastDto.builder()
                .id(broadcast.getId())
                .message(broadcast.getMessage())
                .imageUrl(broadcast.getImageUrl())
                .recipientCriteria(broadcast.getRecipientCriteria())
                .createdBy(broadcast.getCreatedBy())
                .status(broadcast.getStatus())
                .totalRecipients(broadcast.getTotalRecipients())
                .successfulCount(broadcast.getSuccessfulCount())
                .failedCount(broadcast.getFailedCount())
                .createdAt(broadcast.getCreatedAt())
                .build();
    }

    public BroadcastRecipientDto toDto(BroadcastRecipient recipient) {
        return BroadcastRecipientDto.builder()
                .id(recipient.getId())
                .contributorId(recipient.getContributor().getId())
                .contributorName(recipient.getContributor().getName())
                .phoneNumber(recipient.getPhoneNumber())
                .status(recipient.getStatus())
                .providerMessageId(recipient.getProviderMessageId())
                .errorMessage(recipient.getErrorMessage())
                .sentAt(recipient.getSentAt())
                .build();
    }
}
