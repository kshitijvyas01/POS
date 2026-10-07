package com.medipoint.service;

import com.medipoint.dto.MedicineDto;
import com.medipoint.dto.MedicineRequestDto;
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

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MedicineService {

    private final MedicineRepository medicineRepository;
    private final StockMovementRepository stockMovementRepository;

    public MedicineService(MedicineRepository medicineRepository,
                           StockMovementRepository stockMovementRepository) {
        this.medicineRepository = medicineRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> getMedicines(String query, String status, int page, int size, boolean isOwner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String trimmedQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;
        String trimmedStatus = (status != null && !status.trim().isEmpty()) ? status.trim().toUpperCase() : null;

        return medicineRepository.searchMedicines(trimmedQuery, trimmedStatus, pageable)
                .map(m -> MedicineDto.fromEntity(m, isOwner));
    }

    @Transactional(readOnly = true)
    public MedicineDto getMedicineById(Long id, boolean isOwner) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + id));
        return MedicineDto.fromEntity(medicine, isOwner);
    }

    @Transactional
    public MedicineDto createMedicine(MedicineRequestDto request, String username) {
        String cleanName = request.getName().trim();
        String cleanBatch = request.getBatchNumber().trim();

        if (medicineRepository.existsByNameIgnoreCaseAndBatchNumberIgnoreCase(cleanName, cleanBatch)) {
            throw CustomException.conflict("Medicine '" + cleanName + "' with batch number '" + cleanBatch + "' already exists in inventory.");
        }

        if (request.getManufacturingDate() != null && request.getExpiryDate() != null) {
            if (request.getManufacturingDate().isAfter(request.getExpiryDate())) {
                throw CustomException.badRequest("Manufacturing date cannot be after expiry date.");
            }
        }

        Medicine medicine = new Medicine();
        medicine.setName(cleanName);
        medicine.setGenericName(request.getGenericName());
        medicine.setBrand(request.getBrand());
        medicine.setCategory(request.getCategory());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setBatchNumber(cleanBatch);
        medicine.setManufacturingDate(request.getManufacturingDate());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setPurchasePrice(request.getPurchasePrice());
        medicine.setSellingPrice(request.getSellingPrice());
        medicine.setCurrentStock(request.getCurrentStock() != null ? request.getCurrentStock() : 0);
        medicine.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 10);
        medicine.setUnit(request.getUnit() != null ? request.getUnit() : "Strip");
        medicine.setSupplierId(request.getSupplierId());
        medicine.setSupplierName(request.getSupplierName());
        medicine.setDescription(request.getDescription());
        medicine.setStatus(request.getStatus() != null ? request.getStatus().toUpperCase() : "ACTIVE");
        medicine.setCreatedBy(username);
        medicine.setUpdatedBy(username);

        Medicine saved = medicineRepository.save(medicine);

        // Record initial stock movement if stock > 0
        if (saved.getCurrentStock() > 0) {
            StockMovement movement = new StockMovement(
                    saved,
                    saved.getName(),
                    saved.getBatchNumber(),
                    StockMovementType.ADJUSTMENT,
                    saved.getCurrentStock(),
                    0,
                    saved.getCurrentStock(),
                    "INIT-" + saved.getId(),
                    "Initial stock entry upon medicine registration",
                    username
            );
            stockMovementRepository.save(movement);
        }

        return MedicineDto.fromEntity(saved, true);
    }

    @Transactional
    public MedicineDto updateMedicine(Long id, MedicineRequestDto request, String username) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + id));

        String cleanName = request.getName().trim();
        String cleanBatch = request.getBatchNumber().trim();

        if (medicineRepository.existsByNameIgnoreCaseAndBatchNumberIgnoreCaseAndIdNot(cleanName, cleanBatch, id)) {
            throw CustomException.conflict("Medicine '" + cleanName + "' with batch number '" + cleanBatch + "' already exists.");
        }

        if (request.getManufacturingDate() != null && request.getExpiryDate() != null) {
            if (request.getManufacturingDate().isAfter(request.getExpiryDate())) {
                throw CustomException.badRequest("Manufacturing date cannot be after expiry date.");
            }
        }

        int previousStock = medicine.getCurrentStock();
        int targetStock = request.getCurrentStock() != null ? request.getCurrentStock() : previousStock;

        medicine.setName(cleanName);
        medicine.setGenericName(request.getGenericName());
        medicine.setBrand(request.getBrand());
        medicine.setCategory(request.getCategory());
        medicine.setManufacturer(request.getManufacturer());
        medicine.setBatchNumber(cleanBatch);
        medicine.setManufacturingDate(request.getManufacturingDate());
        medicine.setExpiryDate(request.getExpiryDate());
        medicine.setPurchasePrice(request.getPurchasePrice());
        medicine.setSellingPrice(request.getSellingPrice());
        medicine.setCurrentStock(targetStock);
        medicine.setMinimumStock(request.getMinimumStock() != null ? request.getMinimumStock() : 10);
        medicine.setUnit(request.getUnit() != null ? request.getUnit() : "Strip");
        medicine.setSupplierId(request.getSupplierId());
        medicine.setSupplierName(request.getSupplierName());
        medicine.setDescription(request.getDescription());
        if (request.getStatus() != null) {
            medicine.setStatus(request.getStatus().toUpperCase());
        }
        medicine.setUpdatedBy(username);

        Medicine updated = medicineRepository.save(medicine);

        // Record stock movement if stock was changed during update
        if (previousStock != targetStock) {
            int difference = targetStock - previousStock;
            StockMovement movement = new StockMovement(
                    updated,
                    updated.getName(),
                    updated.getBatchNumber(),
                    StockMovementType.ADJUSTMENT,
                    Math.abs(difference),
                    previousStock,
                    targetStock,
                    "EDIT-" + updated.getId(),
                    "Stock modified via medicine edit form",
                    username
            );
            stockMovementRepository.save(movement);
        }

        return MedicineDto.fromEntity(updated, true);
    }

    @Transactional
    public MedicineDto toggleStatus(Long id, String username) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + id));

        String newStatus = "ACTIVE".equalsIgnoreCase(medicine.getStatus()) ? "INACTIVE" : "ACTIVE";
        medicine.setStatus(newStatus);
        medicine.setUpdatedBy(username);

        Medicine updated = medicineRepository.save(medicine);
        return MedicineDto.fromEntity(updated, true);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        Medicine medicine = medicineRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + id));
        // Soft delete / deactivate to preserve historical foreign key integrity
        medicine.setStatus("INACTIVE");
        medicineRepository.save(medicine);
    }

    @Transactional(readOnly = true)
    public List<MedicineDto> searchForPos(String query) {
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : "";
        Pageable pageable = PageRequest.of(0, 30, Sort.by("name").ascending());
        List<Medicine> results = medicineRepository.searchForPos(cleanQuery, LocalDate.now(), pageable);
        // POS search result is served to workers/cashiers: confidential financial data must be masked!
        return results.stream()
                .map(m -> MedicineDto.fromEntity(m, false))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> getLowStock(int page, int size, boolean isOwner) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("currentStock").ascending());
        return medicineRepository.findLowStock(pageable).map(m -> MedicineDto.fromEntity(m, isOwner));
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> getExpiringSoon(int daysThreshold, int page, int size, boolean isOwner) {
        LocalDate today = LocalDate.now();
        LocalDate threshold = today.plusDays(daysThreshold > 0 ? daysThreshold : 30);
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").ascending());
        return medicineRepository.findExpiringSoon(today, threshold, pageable).map(m -> MedicineDto.fromEntity(m, isOwner));
    }

    @Transactional(readOnly = true)
    public Page<MedicineDto> getExpired(int page, int size, boolean isOwner) {
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page, size, Sort.by("expiryDate").descending());
        return medicineRepository.findExpired(today, pageable).map(m -> MedicineDto.fromEntity(m, isOwner));
    }
}
