package com.medipoint.controller;

import com.medipoint.dto.*;
import com.medipoint.model.UserRole;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/inventory", "/api/inventory"})
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<InventorySummaryDto>> getInventorySummary(
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        InventorySummaryDto summary = inventoryService.getSummary(isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Inventory summary retrieved successfully.", summary));
    }

    @GetMapping("/items")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getInventoryItems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        Page<MedicineDto> items = inventoryService.getInventoryItems(query, status, page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Inventory items retrieved successfully.", items));
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<StockMovementResponseDto>> adjustStock(
            @Valid @RequestBody StockAdjustmentDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String username = principal != null ? principal.getUsername() : "system";
        StockMovementResponseDto movement = inventoryService.adjustStock(request, username);
        return ResponseEntity.ok(ApiResponse.ok("Stock adjustment executed successfully. Movement logged.", movement));
    }

    @GetMapping("/movements")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Page<StockMovementResponseDto>>> getStockMovements(
            @RequestParam(required = false) Long medicineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<StockMovementResponseDto> movements = inventoryService.getStockMovements(medicineId, page, size);
        return ResponseEntity.ok(ApiResponse.ok("Stock movements retrieved successfully.", movements));
    }
}
