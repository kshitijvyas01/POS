package com.medipoint.dto;

import com.medipoint.model.Sale;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class SaleResponseDto {
    private Long id;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private Long workerId;
    private String workerName;
    private Integer itemsCount;
    private BigDecimal subtotal;
    private BigDecimal discount;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private BigDecimal totalProfit; // Hidden/null for Worker users
    private String paymentMethod;
    private String paymentStatus;
    private LocalDateTime saleDate;
    private LocalDateTime createdAt;
    private String createdBy;
    private List<SaleItemResponseDto> items;

    public SaleResponseDto() {}

    public static SaleResponseDto fromEntity(Sale s, boolean isOwner) {
        if (s == null) return null;
        SaleResponseDto dto = new SaleResponseDto();
        dto.setId(s.getId());
        dto.setInvoiceNumber(s.getInvoiceNumber());
        dto.setCustomerId(s.getCustomerId());
        dto.setCustomerName(s.getCustomerName());
        dto.setCustomerPhone(s.getCustomerPhone());
        dto.setCustomerEmail(s.getCustomerEmail());
        dto.setWorkerId(s.getWorkerId());
        dto.setWorkerName(s.getWorkerName());
        dto.setItemsCount(s.getItemsCount());
        dto.setSubtotal(s.getSubtotal());
        dto.setDiscount(s.getDiscount());
        dto.setTax(s.getTax());
        dto.setTotalAmount(s.getTotalAmount());
        dto.setTotalProfit(isOwner ? s.getTotalProfit() : null); // Confidential: Owner only
        dto.setPaymentMethod(s.getPaymentMethod());
        dto.setPaymentStatus(s.getPaymentStatus());
        dto.setSaleDate(s.getSaleDate());
        dto.setCreatedAt(s.getCreatedAt());
        dto.setCreatedBy(s.getCreatedBy());

        if (s.getItems() != null) {
            dto.setItems(s.getItems().stream()
                    .map(item -> SaleItemResponseDto.fromEntity(item, isOwner))
                    .collect(Collectors.toList()));
        }
        return dto;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInvoiceNumber() { return invoiceNumber; }
    public void setInvoiceNumber(String invoiceNumber) { this.invoiceNumber = invoiceNumber; }

    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public Long getWorkerId() { return workerId; }
    public void setWorkerId(Long workerId) { this.workerId = workerId; }

    public String getWorkerName() { return workerName; }
    public void setWorkerName(String workerName) { this.workerName = workerName; }

    public Integer getItemsCount() { return itemsCount; }
    public void setItemsCount(Integer itemsCount) { this.itemsCount = itemsCount; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public BigDecimal getTotalProfit() { return totalProfit; }
    public void setTotalProfit(BigDecimal totalProfit) { this.totalProfit = totalProfit; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getSaleDate() { return saleDate; }
    public void setSaleDate(LocalDateTime saleDate) { this.saleDate = saleDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public List<SaleItemResponseDto> getItems() { return items; }
    public void setItems(List<SaleItemResponseDto> items) { this.items = items; }
}
