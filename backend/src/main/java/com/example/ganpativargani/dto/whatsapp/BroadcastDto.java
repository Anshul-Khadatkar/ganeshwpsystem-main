package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.entity.BroadcastStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class BroadcastDto {
    private Long id;
    private String message;
    private String imageUrl;
    private String recipientCriteria;
    private String createdBy;
    private BroadcastStatus status;
    private int totalRecipients;
    private int successfulCount;
    private int failedCount;
    private LocalDateTime createdAt;
}
