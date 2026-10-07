package com.medipoint.service;

import com.medipoint.dto.StoreSettingsDto;
import com.medipoint.model.StoreSettings;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class SettingsService {

    private StoreSettings currentSettings;

    public SettingsService() {
        this.currentSettings = StoreSettings.createDefault();
    }

    public StoreSettingsDto getSettings() {
        return mapToDto(currentSettings);
    }

    public StoreSettingsDto updateSettings(StoreSettingsDto dto, String updatedBy) {
        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        if (dto.getStoreName() != null) currentSettings.setStoreName(dto.getStoreName().trim());
        if (dto.getTagline() != null) currentSettings.setTagline(dto.getTagline().trim());
        if (dto.getAddress() != null) currentSettings.setAddress(dto.getAddress().trim());
        if (dto.getPhone() != null) currentSettings.setPhone(dto.getPhone().trim());
        if (dto.getEmail() != null) currentSettings.setEmail(dto.getEmail().trim());
        if (dto.getLicenseNumber() != null) currentSettings.setLicenseNumber(dto.getLicenseNumber().trim());
        if (dto.getGstNumber() != null) currentSettings.setGstNumber(dto.getGstNumber().trim());
        if (dto.getUpiId() != null) currentSettings.setUpiId(dto.getUpiId().trim());
        if (dto.getInvoicePrefix() != null) currentSettings.setInvoicePrefix(dto.getInvoicePrefix().trim());
        if (dto.getInvoiceFooter() != null) currentSettings.setInvoiceFooter(dto.getInvoiceFooter().trim());
        currentSettings.setUpdatedAt(now);
        currentSettings.setUpdatedBy(updatedBy);

        return mapToDto(currentSettings);
    }

    private StoreSettingsDto mapToDto(StoreSettings s) {
        return new StoreSettingsDto(
                s.getStoreName(),
                s.getTagline(),
                s.getAddress(),
                s.getPhone(),
                s.getEmail(),
                s.getLicenseNumber(),
                s.getGstNumber(),
                s.getUpiId(),
                s.getQrCodeUrl(),
                s.getInvoicePrefix(),
                s.getInvoiceFooter(),
                s.getUpdatedAt(),
                s.getUpdatedBy()
        );
    }
}
