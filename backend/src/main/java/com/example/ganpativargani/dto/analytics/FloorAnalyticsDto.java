package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class FloorAnalyticsDto {
    private String floorNumber;
    private BigDecimal totalDonation;
    private long donorCount;
}
