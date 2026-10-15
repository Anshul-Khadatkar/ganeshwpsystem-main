package com.example.ganpativargani.controller;

import com.example.ganpativargani.dto.AppSettingsDto;
import com.example.ganpativargani.dto.AppSettingsRequest;
import com.example.ganpativargani.service.AppSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Non-secret settings only. WhatsApp access token / phone number ID /
 * business account ID / API version remain environment variables - see
 * Section 33 - and are never exposed or editable through this API.
 */
@RestController
@RequestMapping("/api/settings")
@RequiredArgsConstructor
public class SettingsController {

    private final AppSettingsService settingsService;

    @GetMapping
    public AppSettingsDto get() {
        return AppSettingsDto.builder()
                .organizationName(settingsService.getOrganizationName())
                .defaultImageUrl(settingsService.getDefaultImageUrl())
                .build();
    }

    @PutMapping
    public AppSettingsDto update(@Valid @RequestBody AppSettingsRequest request) {
        settingsService.setOrganizationName(request.getOrganizationName().trim());
        settingsService.setDefaultImageUrl(request.getDefaultImageUrl());
        return get();
    }
}
