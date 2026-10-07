package com.medipoint.controller;

import com.medipoint.dto.ApiResponse;
import com.medipoint.dto.StoreSettingsDto;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.SettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/settings", "/api/settings"})
public class SettingsController {

    private final SettingsService settingsService;

    public SettingsController(SettingsService settingsService) {
        this.settingsService = settingsService;
    }

    @GetMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<StoreSettingsDto>> getStoreSettings() {
        StoreSettingsDto settings = settingsService.getSettings();
        return ResponseEntity.ok(ApiResponse.ok("Store settings fetched successfully.", settings));
    }

    @PutMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<StoreSettingsDto>> updateStoreSettings(
            @RequestBody StoreSettingsDto dto,
            @AuthenticationPrincipal UserPrincipal principal) {
        String updatedBy = (principal != null) ? principal.getUsername() : "system";
        StoreSettingsDto updated = settingsService.updateSettings(dto, updatedBy);
        return ResponseEntity.ok(ApiResponse.ok("Store settings updated successfully.", updated));
    }
}
