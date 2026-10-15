package com.example.ganpativargani.dto.analytics;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ThresholdBucketDto {
    private String label;
    private long donationCount;
}
