package com.medipoint.dto;

import java.math.BigDecimal;

public class InventorySummaryDto {
    private long totalMedicines;
    private long totalStockUnits;
    private long lowStockCount;
    private long outOfStockCount;
    private long expiringSoonCount;
    private long expiredCount;
    private BigDecimal totalStockValue; // Confidential: Owner only

    public InventorySummaryDto() {}

    public InventorySummaryDto(long totalMedicines, long totalStockUnits, long lowStockCount,
                               long outOfStockCount, long expiringSoonCount, long expiredCount,
                               BigDecimal totalStockValue) {
        this.totalMedicines = totalMedicines;
        this.totalStockUnits = totalStockUnits;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.expiringSoonCount = expiringSoonCount;
        this.expiredCount = expiredCount;
        this.totalStockValue = totalStockValue;
    }

    public long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(long totalMedicines) { this.totalMedicines = totalMedicines; }

    public long getTotalStockUnits() { return totalStockUnits; }
    public void setTotalStockUnits(long totalStockUnits) { this.totalStockUnits = totalStockUnits; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }

    public long getExpiringSoonCount() { return expiringSoonCount; }
    public void setExpiringSoonCount(long expiringSoonCount) { this.expiringSoonCount = expiringSoonCount; }

    public long getExpiredCount() { return expiredCount; }
    public void setExpiredCount(long expiredCount) { this.expiredCount = expiredCount; }

    public BigDecimal getTotalStockValue() { return totalStockValue; }
    public void setTotalStockValue(BigDecimal totalStockValue) { this.totalStockValue = totalStockValue; }
}
