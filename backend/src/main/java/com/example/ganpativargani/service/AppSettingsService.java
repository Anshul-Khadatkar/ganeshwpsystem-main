package com.example.ganpativargani.service;

import com.example.ganpativargani.entity.AppSetting;
import com.example.ganpativargani.repository.AppSettingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AppSettingsService {

    private static final String KEY_ORGANIZATION_NAME = "organizationName";
    private static final String KEY_DEFAULT_IMAGE_URL = "defaultImageUrl";

    private final AppSettingRepository repository;

    @Value("${app.organization.name}")
    private String organizationNameFallback;

    public AppSettingsService(AppSettingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public String getOrganizationName() {
        return get(KEY_ORGANIZATION_NAME).orElse(organizationNameFallback);
    }

    @Transactional
    public void setOrganizationName(String value) {
        set(KEY_ORGANIZATION_NAME, value);
    }

    @Transactional(readOnly = true)
    public String getDefaultImageUrl() {
        return get(KEY_DEFAULT_IMAGE_URL).orElse(null);
    }

    @Transactional
    public void setDefaultImageUrl(String value) {
        set(KEY_DEFAULT_IMAGE_URL, value);
    }

    @Transactional(readOnly = true)
    public Optional<String> get(String key) {
        return repository.findBySettingKey(key).map(AppSetting::getSettingValue);
    }

    @Transactional
    public void set(String key, String value) {
        AppSetting setting = repository.findBySettingKey(key)
                .orElseGet(() -> AppSetting.builder().settingKey(key).build());
        setting.setSettingValue(value);
        setting.setUpdatedAt(LocalDateTime.now());
        repository.save(setting);
    }
}
