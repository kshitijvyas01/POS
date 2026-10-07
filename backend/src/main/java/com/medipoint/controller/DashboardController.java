package com.medipoint.controller;

import com.medipoint.dto.ApiResponse;
import com.medipoint.dto.DashboardSummaryDto;
import com.medipoint.model.UserRole;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/dashboard", "/api/dashboard"})
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> getDashboardSummary(@AuthenticationPrincipal UserPrincipal principal) {
        UserRole role = (principal != null) ? principal.getRole() : UserRole.WORKER;
        DashboardSummaryDto summary = dashboardService.getSummaryForRole(role);
        return ResponseEntity.ok(ApiResponse.ok("Dashboard summary retrieved successfully.", summary));
    }
}
