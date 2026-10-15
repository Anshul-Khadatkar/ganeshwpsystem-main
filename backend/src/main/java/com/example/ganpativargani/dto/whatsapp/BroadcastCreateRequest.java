package com.example.ganpativargani.dto.whatsapp;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class BroadcastCreateRequest {

    /** Optional - if set, message/imageUrl below are treated as the (possibly edited) resolved text to send. */
    private Long templateId;

    @NotBlank(message = "Message is required")
    private String message;

    /** Always optional - a broadcast can be text-only. */
    private String imageUrl;

    @NotNull(message = "Recipient mode is required")
    private RecipientMode recipientMode;

    // --- used when recipientMode = FILTERED ---
    private Long buildingId;
    private BigDecimal minDonationAmount;
    private LocalDate donatedFrom;
    private LocalDate donatedTo;

    // --- used when recipientMode = EXPLICIT ---
    private List<Long> contributorIds;

    public enum RecipientMode {
        ALL,
        FILTERED,
        EXPLICIT
    }
}
