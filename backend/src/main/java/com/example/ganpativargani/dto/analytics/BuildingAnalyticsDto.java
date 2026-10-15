package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class BuildingAnalyticsDto {
    private Long buildingId;
    private String buildingName;
    private BigDecimal totalDonation;
    private long totalDonors;
    private BigDecimal averageDonation;
    private BigDecimal highestDonation;
    private BigDecimal lowestDonation;
    private boolean requiresFloor;
    private boolean requiresFlat;
}
