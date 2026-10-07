package com.medipoint.service;

import com.medipoint.dto.InventorySummaryDto;
import com.medipoint.dto.MedicineDto;
import com.medipoint.dto.StockAdjustmentDto;
import com.medipoint.dto.StockMovementResponseDto;
import com.medipoint.exception.CustomException;
import com.medipoint.model.Medicine;
import com.medipoint.model.StockMovement;
import com.medipoint.model.StockMovementType;
import com.medipoint.repository.MedicineRepository;
import com.medipoint.repository.StockMovementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
public class InventoryService {

    private final MedicineRepository medicineRepository;
    private final StockMovementRepository stockMovementRepository;

    public InventoryService(MedicineRepository medicineRepository,
                            StockMovementRepository stockMovementRepository) {
        this.medicineRepository = medicineRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public InventorySummaryDto getSummary(boolean isOwner) {
        LocalDate today = LocalDate.now();
        LocalDate expiringThreshold = today.plusDays(30);

        long totalMedicines = medicineRepository.countByStatus("ACTIVE");
        long totalStockUnits = medicineRepository.sumTotalStockUnits();
        long lowStockCount = medicineRepository.countLowStock();
        long outOfStockCount = medicineRepository.countOutOfStock();
        long expiringSoonCount = medicineRepository.countExpiringSoon(today, expiringThreshold);
        long expiredCount = medicineRepository.countExpired(today);

        BigDecimal totalStockValue = null;
        if (isOwner) {
            // Calculate total inventory valuation: sum(purchasePrice * currentStock)
            List<Medicine> allActive = medicineRepository.findAll();
            totalStockValue = allActive.stream()
                    .filter(m -> "ACTIVE".equalsIgnoreCase(m.getStatus()))
                    .map(m -> m.getPurchasePrice().multiply(BigDecimal.valueOf(m.getCurrentStock())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        return new InventorySummaryDto(
                totalMedicines,
                totalStockUnits,
                lowStockCount,
                outOfStockCount,
                expiringSoonCount,
                expiredCount,
                totalStockValue
        );
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> getInventoryItems(String query, String status, int page, int size, boolean isOwner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String trimmedStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : null;

        return medicineRepository.searchMedicines(trimmedQuery, trimmedStatus, pageable)
                .map(m -> MedicineDto.fromEntity(m, isOwner));
    }

    @Transactional
    public StockMovementResponseDto adjustStock(StockAdjustmentDto request, String username) {
        Medicine medicine = medicineRepository.findById(request.getMedicineId())
                .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + request.getMedicineId()));

        if (request.getNewStock() < 0) {
            throw CustomException.badRequest("Stock cannot be negative.");
        }

        int previousStock = medicine.getCurrentStock();
        int newStock = request.getNewStock();
        int difference = newStock - previousStock;

        medicine.setCurrentStock(newStock);
        medicine.setUpdatedBy(username);
        medicineRepository.save(medicine);

        StockMovement movement = new StockMovement(
                medicine,
                medicine.getName(),
                medicine.getBatchNumber(),
                StockMovementType.ADJUSTMENT,
                Math.abs(difference),
                previousStock,
                newStock,
                "ADJ-" + System.currentTimeMillis(),
                request.getReason(),
                username
        );

        StockMovement savedMovement = stockMovementRepository.save(movement);
        return StockMovementResponseDto.fromEntity(savedMovement);
    }

    @Transactional(readOnly = true)
    public Page<StockMovementResponseDto> getStockMovements(Long medicineId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (medicineId != null && medicineId > 0) {
            return stockMovementRepository.findByMedicineIdOrderByCreatedAtDesc(medicineId, pageable)
                    .map(StockMovementResponseDto::fromEntity);
        }
        return stockMovementRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(StockMovementResponseDto::fromEntity);
    }
}
