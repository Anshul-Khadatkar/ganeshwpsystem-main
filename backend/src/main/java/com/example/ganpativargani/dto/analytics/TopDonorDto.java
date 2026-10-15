package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class TopDonorDto {
    private Long contributorId;
    private String contributorName;
    private String buildingName;
    private BigDecimal totalDonation;
    private long donationCount;
}
