package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class WhatsAppStatsDto {
    private long sent;
    private long pending;
    private long failed;
    private long notSent;
}
