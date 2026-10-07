package com.medipoint.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class SaleRequestDto {

    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;
    private String paymentMethod = "CASH"; // "CASH", "UPI", "CARD", "OTHER"
    private String paymentStatus = "PAID"; // "PAID", "PENDING"

    @NotEmpty(message = "Cart cannot be empty")
    @Valid
    private List<SaleItemRequestDto> items;

    public SaleRequestDto() {}

    public SaleRequestDto(String customerName, String customerPhone, String customerEmail,
                          BigDecimal discount, BigDecimal tax, String paymentMethod,
                          String paymentStatus, List<SaleItemRequestDto> items) {
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.discount = discount != null ? discount : BigDecimal.ZERO;
        this.tax = tax != null ? tax : BigDecimal.ZERO;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PAID";
        this.items = items;
    }

    public String getCustomerName() { return customerName; }
    public void setCustomerName(String customerName) { this.customerName = customerName; }

    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String customerPhone) { this.customerPhone = customerPhone; }

    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String customerEmail) { this.customerEmail = customerEmail; }

    public BigDecimal getDiscount() { return discount; }
    public void setDiscount(BigDecimal discount) { this.discount = discount; }

    public BigDecimal getTax() { return tax; }
    public void setTax(BigDecimal tax) { this.tax = tax; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public List<SaleItemRequestDto> getItems() { return items; }
    public void setItems(List<SaleItemRequestDto> items) { this.items = items; }
}
