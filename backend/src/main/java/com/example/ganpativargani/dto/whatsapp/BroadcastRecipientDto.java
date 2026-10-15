package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.WhatsAppStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BroadcastRecipientDto {
    private Long id;
    private Long contributorId;
    private String contributorName;
    private String phoneNumber;
    private WhatsAppStatus status;
    private String providerMessageId;
    private String errorMessage;
    private LocalDateTime sentAt;
}
