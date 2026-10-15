package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class SummaryStatsDto {
    private BigDecimal totalDonation;
    private long totalDonors;
    private long totalDonations;
    private BigDecimal averageDonation;
    private BigDecimal highestDonation;
    private BigDecimal lowestDonation;
}
