package com.medipoint.dto;

import com.medipoint.model.StockMovement;
import com.medipoint.model.StockMovementType;
import java.time.LocalDateTime;

public class StockMovementResponseDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private StockMovementType type;
    private Integer quantity;
    private Integer previousStock;
    private Integer newStock;
    private String referenceId;
    private String reason;
    private LocalDateTime createdAt;
    private String createdBy;

    public StockMovementResponseDto() {}

    public static StockMovementResponseDto fromEntity(StockMovement m) {
        if (m == null) return null;
        StockMovementResponseDto dto = new StockMovementResponseDto();
        dto.setId(m.getId());
        dto.setMedicineId(m.getMedicine() != null ? m.getMedicine().getId() : null);
        dto.setMedicineName(m.getMedicineName());
        dto.setBatchNumber(m.getBatchNumber());
        dto.setType(m.getType());
        dto.setQuantity(m.getQuantity());
        dto.setPreviousStock(m.getPreviousStock());
        dto.setNewStock(m.getNewStock());
        dto.setReferenceId(m.getReferenceId());
        dto.setReason(m.getReason());
        dto.setCreatedAt(m.getCreatedAt());
        dto.setCreatedBy(m.getCreatedBy());
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public StockMovementType getType() { return type; }
    public void setType(StockMovementType type) { this.type = type; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public Integer getPreviousStock() { return previousStock; }
    public void setPreviousStock(Integer previousStock) { this.previousStock = previousStock; }

    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }

    public String getReferenceId() { return referenceId; }
    public void setReferenceId(String referenceId) { this.referenceId = referenceId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
}
