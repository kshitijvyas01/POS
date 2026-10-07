package com.medipoint.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MedicineRequestDto {

    @NotBlank(message = "Medicine name is required")
    private String name;

    private String genericName;
    private String brand;
    private String category;
    private String manufacturer;

    @NotBlank(message = "Batch number is required")
    private String batchNumber;

    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    private LocalDate expiryDate;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price must be non-negative")
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price must be non-negative")
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @Min(value = 0, message = "Current stock cannot be negative")
    private Integer currentStock = 0;

    @Min(value = 0, message = "Minimum stock cannot be negative")
    private Integer minimumStock = 10;

    private String unit = "Strip";
    private String supplierId;
    private String supplierName;
    private String description;
    private String status = "ACTIVE";

    public MedicineRequestDto() {}

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
}
