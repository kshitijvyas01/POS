package com.medipoint.dto;

import com.medipoint.model.SaleItem;
import java.math.BigDecimal;

public class SaleItemResponseDto {
    private Long id;
    private Long medicineId;
    private String medicineName;
    private String batchNumber;
    private Integer quantity;
    private BigDecimal sellingPrice;
    private BigDecimal subtotal;
    private BigDecimal purchasePrice; // Null/hidden for Worker users
    private BigDecimal profit;        // Null/hidden for Worker users

    public SaleItemResponseDto() {}

    public static SaleItemResponseDto fromEntity(SaleItem item, boolean isOwner) {
        if (item == null) return null;
        SaleItemResponseDto dto = new SaleItemResponseDto();
        dto.setId(item.getId());
        dto.setMedicineId(item.getMedicine() != null ? item.getMedicine().getId() : null);
        dto.setMedicineName(item.getMedicineName());
        dto.setBatchNumber(item.getBatchNumber());
        dto.setQuantity(item.getQuantity());
        dto.setSellingPrice(item.getSellingPrice());
        dto.setSubtotal(item.getSubtotal());
        dto.setPurchasePrice(isOwner ? item.getPurchasePrice() : null); // Confidential: Owner only
        dto.setProfit(isOwner ? item.getProfit() : null);               // Confidential: Owner only
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

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSellingPrice() { return sellingPrice; }
    public void setSellingPrice(BigDecimal sellingPrice) { this.sellingPrice = sellingPrice; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getPurchasePrice() { return purchasePrice; }
    public void setPurchasePrice(BigDecimal purchasePrice) { this.purchasePrice = purchasePrice; }

    public BigDecimal getProfit() { return profit; }
    public void setProfit(BigDecimal profit) { this.profit = profit; }
}
