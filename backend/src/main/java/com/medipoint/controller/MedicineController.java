package com.medipoint.controller;

import com.medipoint.dto.ApiResponse;
import com.medipoint.dto.MedicineDto;
import com.medipoint.dto.MedicineRequestDto;
import com.medipoint.model.UserRole;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.MedicineService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/api/v1/medicines", "/api/medicines"})
public class MedicineController {

    private final MedicineService medicineService;

    public MedicineController(MedicineService medicineService) {
        this.medicineService = medicineService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getMedicines(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        Page<MedicineDto> medicines = medicineService.getMedicines(query, status, page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Medicines retrieved successfully.", medicines));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<MedicineDto>> getMedicineById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        MedicineDto medicine = medicineService.getMedicineById(id, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Medicine details retrieved successfully.", medicine));
    }

    @PostMapping
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MedicineDto>> createMedicine(
            @Valid @RequestBody MedicineRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String username = principal != null ? principal.getUsername() : "system";
        MedicineDto created = medicineService.createMedicine(request, username);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Medicine created successfully.", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MedicineDto>> updateMedicine(
            @PathVariable Long id,
            @Valid @RequestBody MedicineRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        String username = principal != null ? principal.getUsername() : "system";
        MedicineDto updated = medicineService.updateMedicine(id, request, username);
        return ResponseEntity.ok(ApiResponse.ok("Medicine updated successfully.", updated));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<MedicineDto>> toggleStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        String username = principal != null ? principal.getUsername() : "system";
        MedicineDto updated = medicineService.toggleStatus(id, username);
        return ResponseEntity.ok(ApiResponse.ok("Medicine status updated to: " + updated.getStatus(), updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('OWNER')")
    public ResponseEntity<ApiResponse<Void>> deleteMedicine(@PathVariable Long id) {
        medicineService.deleteMedicine(id);
        return ResponseEntity.ok(ApiResponse.ok("Medicine deactivated successfully.", null));
    }

    @GetMapping("/pos-search")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<List<MedicineDto>>> searchForPos(@RequestParam(required = false) String query) {
        List<MedicineDto> results = medicineService.searchForPos(query);
        return ResponseEntity.ok(ApiResponse.ok("POS search results retrieved.", results));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getLowStock(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        Page<MedicineDto> list = medicineService.getLowStock(page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Low stock medicines retrieved.", list));
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getExpiringSoon(
            @RequestParam(defaultValue = "30") int days,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        Page<MedicineDto> list = medicineService.getExpiringSoon(days, page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Expiring medicines retrieved.", list));
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<MedicineDto>>> getExpired(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        Page<MedicineDto> list = medicineService.getExpired(page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Expired medicines retrieved.", list));
    }
}
