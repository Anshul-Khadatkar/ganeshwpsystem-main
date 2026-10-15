package com.example.ganpativargani.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppSettingsRequest {

    @NotBlank(message = "Organization name is required")
    private String organizationName;

    /** Always optional. */
    private String defaultImageUrl;
}
