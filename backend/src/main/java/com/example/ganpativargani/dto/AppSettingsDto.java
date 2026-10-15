package com.example.ganpativargani.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AppSettingsDto {
    private String organizationName;
    private String defaultImageUrl;
}
