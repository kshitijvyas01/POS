package com.medipoint.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class StockAdjustmentDto {

    @NotNull(message = "Medicine ID is required")
    private Long medicineId;

    @NotNull(message = "New stock quantity is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer newStock;

    @NotBlank(message = "Adjustment reason is required (e.g. Damaged, Lost, Physical count)")
    private String reason;

    public StockAdjustmentDto() {}

    public StockAdjustmentDto(Long medicineId, Integer newStock, String reason) {
        this.medicineId = medicineId;
        this.newStock = newStock;
        this.reason = reason;
    }

    public Long getMedicineId() { return medicineId; }
    public void setMedicineId(Long medicineId) { this.medicineId = medicineId; }

    public Integer getNewStock() { return newStock; }
    public void setNewStock(Integer newStock) { this.newStock = newStock; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
