package com.medipoint.controller;

import com.medipoint.dto.ApiResponse;
import com.medipoint.dto.SaleRequestDto;
import com.medipoint.dto.SaleResponseDto;
import com.medipoint.model.UserRole;
import com.medipoint.security.UserPrincipal;
import com.medipoint.service.SaleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping({"/api/v1/sales", "/api/sales"})
public class SaleController {

    private final SaleService saleService;

    public SaleController(SaleService saleService) {
        this.saleService = saleService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<SaleResponseDto>> checkout(
            @Valid @RequestBody SaleRequestDto request,
            @AuthenticationPrincipal UserPrincipal principal) {

        SaleResponseDto response = saleService.checkout(request, principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Sale completed successfully. Stock deducted.", response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<Page<SaleResponseDto>>> getSales(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Long workerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        // Workers are restricted to seeing their own sales or general history without profit metrics
        Long effectiveWorkerId = isOwner ? workerId : (principal != null ? principal.getId() : null);

        Page<SaleResponseDto> sales = saleService.getSales(query, effectiveWorkerId, startDate, endDate, page, size, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Sales list retrieved successfully.", sales));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<SaleResponseDto>> getSaleById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        SaleResponseDto sale = saleService.getSaleById(id, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Sale details retrieved successfully.", sale));
    }

    @GetMapping("/invoice/{invoiceNumber}")
    @PreAuthorize("hasAnyRole('OWNER', 'WORKER')")
    public ResponseEntity<ApiResponse<SaleResponseDto>> getSaleByInvoiceNumber(
            @PathVariable String invoiceNumber,
            @AuthenticationPrincipal UserPrincipal principal) {

        boolean isOwner = principal != null && principal.getRole() == UserRole.OWNER;
        SaleResponseDto sale = saleService.getSaleByInvoiceNumber(invoiceNumber, isOwner);
        return ResponseEntity.ok(ApiResponse.ok("Sale details retrieved successfully.", sale));
    }
}
