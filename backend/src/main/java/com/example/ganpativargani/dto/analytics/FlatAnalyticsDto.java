package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class FlatAnalyticsDto {
    private Long contributorId;
    private String contributorName;

    /** Null when not applicable (e.g. Row Houses / Shops) - never the literal string "null". */
    private String floorNumber;

    /** Null when not applicable (e.g. Row Houses / Shops) - never the literal string "null". */
    private String flatNumber;

    private BigDecimal totalDonation;
    private long donationCount;
}
