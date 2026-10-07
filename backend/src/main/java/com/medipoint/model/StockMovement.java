package com.medipoint.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "stock_movements",
    indexes = {
        @Index(name = "idx_movement_medicine_id", columnList = "medicine_id"),
        @Index(name = "idx_movement_created_at", columnList = "created_at"),
        @Index(name = "idx_movement_type", columnList = "movement_type")
    }
)
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(name = "medicine_name", length = 150)
    private String medicineName;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private StockMovementType type;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    @Column(name = "reason", length = 255)
    private String reason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    public StockMovement() {}

    public StockMovement(Medicine medicine, String medicineName, String batchNumber,
                         StockMovementType type, Integer quantity, Integer previousStock,
                         Integer newStock, String referenceId, String reason, String createdBy) {
        this.medicine = medicine;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.type = type;
        this.quantity = quantity;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.referenceId = referenceId;
        this.reason = reason;
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

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
