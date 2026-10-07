package com.medipoint.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(
    name = "sale_items",
    indexes = {
        @Index(name = "idx_sale_item_sale_id", columnList = "sale_id"),
        @Index(name = "idx_sale_item_medicine_id", columnList = "medicine_id")
    }
)
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id")
    private Medicine medicine;

    @Column(name = "medicine_name", nullable = false, length = 150)
    private String medicineName;

    @Column(name = "batch_number", nullable = false, length = 50)
    private String batchNumber;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "selling_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal sellingPrice = BigDecimal.ZERO;

    // Confidential: Purchase price at time of sale. Owner only!
    @Column(name = "purchase_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal purchasePrice = BigDecimal.ZERO;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    // Confidential: Calculated as (sellingPrice - purchasePrice) * quantity. Owner only!
    @Column(name = "profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal profit = BigDecimal.ZERO;

    public SaleItem() {}

    public SaleItem(Sale sale, Medicine medicine, String medicineName, String batchNumber,
                    Integer quantity, BigDecimal sellingPrice, BigDecimal purchasePrice,
                    BigDecimal subtotal, BigDecimal profit) {
        this.sale = sale;
        this.medicine = medicine;
        this.medicineName = medicineName;
        this.batchNumber = batchNumber;
        this.quantity = quantity;
        this.sellingPrice = sellingPrice;
        this.purchasePrice = purchasePrice;
        this.subtotal = subtotal;
        this.profit = profit;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public String getMedicineName() { return medicineName; }
    public void setMedicineName(String medicineName) { this.medicineName = medicineName; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }
}
