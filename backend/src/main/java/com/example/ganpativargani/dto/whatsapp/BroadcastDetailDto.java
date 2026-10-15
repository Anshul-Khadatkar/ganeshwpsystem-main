package com.example.ganpativargani.dto.whatsapp;

import com.example.ganpativargani.dto.PageResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class BroadcastDetailDto {
    private BroadcastDto broadcast;
    private PageResponse<BroadcastRecipientDto> recipients;
}
