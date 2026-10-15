package com.example.ganpativargani.dto.whatsapp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class BroadcastPreviewResponse {
    private int recipientCount;
    private String recipientCriteria;
    /** A short sample (first few names) so the admin can sanity-check the filter, not the full list. */
    private List<String> sampleNames;
}
