package com.medipoint.dto;

import com.medipoint.model.Medicine;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineDto {
    private Long id;
    private String name;
    private String genericName;
    private String brand;
    private String category;
    private String manufacturer;
    private String batchNumber;
    private LocalDate manufacturingDate;
    private LocalDate expiryDate;
    private BigDecimal purchasePrice; // Null/hidden for Worker users
    private BigDecimal sellingPrice;
    private Integer currentStock;
    private Integer minimumStock;
    private String unit;
    private String supplierId;
    private String supplierName;
    private String description;
    private String status;
    private boolean active;
    private String stockStatus;
    private boolean expired;
    private boolean expiringSoon;

    public MedicineDto() {}

    public static MedicineDto fromEntity(Medicine m, boolean isOwner) {
        if (m == null) return null;
        MedicineDto dto = new MedicineDto();
        dto.setId(m.getId());
        dto.setName(m.getName());
        dto.setGenericName(m.getGenericName());
        dto.setBrand(m.getBrand());
        dto.setCategory(m.getCategory());
        dto.setManufacturer(m.getManufacturer());
        dto.setBatchNumber(m.getBatchNumber());
        dto.setManufacturingDate(m.getManufacturingDate());
        dto.setExpiryDate(m.getExpiryDate());
        dto.setPurchasePrice(isOwner ? m.getPurchasePrice() : null); // Confidential: Owner only
        dto.setSellingPrice(m.getSellingPrice());
        dto.setCurrentStock(m.getCurrentStock());
        dto.setMinimumStock(m.getMinimumStock());
        dto.setUnit(m.getUnit());
        dto.setSupplierId(m.getSupplierId());
        dto.setSupplierName(m.getSupplierName());
        dto.setDescription(m.getDescription());
        dto.setStatus(m.getStatus());
        dto.setActive("ACTIVE".equalsIgnoreCase(m.getStatus()));
        dto.setStockStatus(m.getCalculatedStockStatus());
        dto.setExpired(m.isExpired());
        dto.setExpiringSoon(m.isExpiringSoon(30));
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGenericName() { return genericName; }
    public void setGenericName(String genericName) { this.genericName = genericName; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getManufacturingDate() { return manufacturingDate; }
    public void setManufacturingDate(LocalDate manufacturingDate) { this.manufacturingDate = manufacturingDate; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public Integer getCurrentStock() { return currentStock; }
    public void setCurrentStock(Integer currentStock) { this.currentStock = currentStock; }

    public Integer getMinimumStock() { return minimumStock; }
    public void setMinimumStock(Integer minimumStock) { this.minimumStock = minimumStock; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getSupplierId() { return supplierId; }
    public void setSupplierId(String supplierId) { this.supplierId = supplierId; }

    public String getSupplierName() { return supplierName; }
    public void setSupplierName(String supplierName) { this.supplierName = supplierName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public String getStockStatus() { return stockStatus; }
    public void setStockStatus(String stockStatus) { this.stockStatus = stockStatus; }

    public boolean isExpired() { return expired; }
    public void setExpired(boolean expired) { this.expired = expired; }

    public boolean isExpiringSoon() { return expiringSoon; }
    public void setExpiringSoon(boolean expiringSoon) { this.expiringSoon = expiringSoon; }
}
