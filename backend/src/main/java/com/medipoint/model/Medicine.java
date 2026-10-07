package com.medipoint.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "medicines",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_medicine_name_batch", columnNames = {"name", "batch_number"})
    },
    indexes = {
        @Index(name = "idx_medicine_name", columnList = "name"),
        @Index(name = "idx_medicine_generic", columnList = "generic_name"),
        @Index(name = "idx_medicine_brand", columnList = "brand"),
        @Index(name = "idx_medicine_batch", columnList = "batch_number"),
        @Index(name = "idx_medicine_expiry", columnList = "expiry_date"),
        @Index(name = "idx_medicine_status", columnList = "status")
    }
)
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medicine name is required")
    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "generic_name", length = 150)
    private String genericName;

    @Column(name = "brand", length = 100)
    private String brand;

    @Column(name = "category", length = 50)
    private String category;

    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @NotBlank(message = "Batch number is required")
    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "manufacturing_date")
    private LocalDate manufacturingDate;

    @NotNull(message = "Expiry date is required")
    @Column(name = "expiry_date", nullable = false)
    private LocalDate expiryDate;

    @NotNull(message = "Purchase price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Purchase price must be non-negative")
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @NotNull(message = "Selling price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Selling price must be non-negative")
    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    @NotNull(message = "Stock cannot be null")
    @Min(value = 0, message = "Current stock cannot be negative")
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @NotNull(message = "Minimum stock cannot be null")
    @Min(value = 0, message = "Minimum stock cannot be negative")
    @Column(name = "minimum_stock", nullable = false)
    private Integer minimumStock = 10;

    @Column(name = "unit", length = 30)
    private String unit = "Strip"; // e.g. "Strip", "Tablet", "Bottle", "Box", "Injection"

    @Column(name = "supplier_id", length = 50)
    private String supplierId;

    @Column(name = "supplier_name", length = 100)
    private String supplierName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "ACTIVE"; // "ACTIVE", "INACTIVE"

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    public Medicine() {}

    public Medicine(String name, String genericName, String brand, String category, String manufacturer,
                    String batchNumber, LocalDate manufacturingDate, LocalDate expiryDate,
                    BigDecimal purchasePrice, BigDecimal sellingPrice, Integer currentStock,
                    Integer minimumStock, String unit, String supplierId, String supplierName,
                    String description, String status) {
        this.name = name;
        this.genericName = genericName;
        this.brand = brand;
        this.category = category;
        this.manufacturer = manufacturer;
        this.batchNumber = batchNumber;
        this.manufacturingDate = manufacturingDate;
        this.expiryDate = expiryDate;
        this.purchasePrice = purchasePrice;
        this.sellingPrice = sellingPrice;
        this.currentStock = currentStock != null ? currentStock : 0;
        this.minimumStock = minimumStock != null ? minimumStock : 10;
        this.unit = unit;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.description = description;
        this.status = status != null ? status : "ACTIVE";
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = "ACTIVE";
        if (this.currentStock == null) this.currentStock = 0;
        if (this.minimumStock == null) this.minimumStock = 10;
        if (this.purchasePrice == null) this.purchasePrice = BigDecimal.ZERO;
        if (this.sellingPrice == null) this.sellingPrice = BigDecimal.ZERO;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiryDate != null && expiryDate.isBefore(LocalDate.now());
    }

    public boolean isExpiringSoon(int daysThreshold) {
        if (expiryDate == null) return false;
        LocalDate now = LocalDate.now();
        return !expiryDate.isBefore(now) && !expiryDate.isAfter(now.plusDays(daysThreshold));
    }

    public String getCalculatedStockStatus() {
        if (isExpired()) {
            return "EXPIRED";
        }
        if (currentStock <= 0) {
            return "OUT_OF_STOCK";
        }
        if (currentStock <= minimumStock) {
            return "LOW_STOCK";
        }
        return "IN_STOCK";
    }

    // Getters and Setters
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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public String getUpdatedBy() { return updatedBy; }
    public void setUpdatedBy(String updatedBy) { this.updatedBy = updatedBy; }
}
