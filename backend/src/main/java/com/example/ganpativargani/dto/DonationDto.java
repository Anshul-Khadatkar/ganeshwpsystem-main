package com.example.ganpativargani.dto;

import com.example.ganpativargani.entity.WhatsAppStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class DonationDto {
    private Long id;
    private ContributorDto contributor;
    private BigDecimal amount;
    private LocalDate donationDate;
    private String description;
    private WhatsAppStatus whatsappStatus;
    private String whatsappMessageId;
    private LocalDateTime whatsappSentAt;
    private String whatsappErrorMessage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
