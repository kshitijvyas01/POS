package com.medipoint.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "sales",
    indexes = {
        @Index(name = "idx_sale_invoice", columnList = "invoice_number", unique = true),
        @Index(name = "idx_sale_date", columnList = "sale_date"),
        @Index(name = "idx_sale_payment_method", columnList = "payment_method")
    }
)
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "invoice_number", nullable = false, unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "customer_name", length = 100)
    private String customerName;

    @Column(name = "customer_phone", length = 20)
    private String customerPhone;

    @Column(name = "customer_email", length = 150)
    private String customerEmail;

    @Column(name = "worker_id")
    private Long workerId;

    @Column(name = "worker_name", length = 100)
    private String workerName;

    @Column(name = "items_count", nullable = false)
    private Integer itemsCount = 0;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "tax", nullable = false, precision = 12, scale = 2)
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    // Confidential: Owner calculations only!
    @Column(name = "total_profit", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalProfit = BigDecimal.ZERO;

    @Column(name = "payment_method", nullable = false, length = 20)
    private String paymentMethod = "CASH"; // "CASH", "UPI", "CARD", "OTHER"

    @Column(name = "payment_status", nullable = false, length = 20)
    private String paymentStatus = "PAID"; // "PAID", "PENDING"

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<SaleItem> items = new ArrayList<>();

    public Sale() {}

    public Sale(String invoiceNumber, Long customerId, String customerName, String customerPhone,
                String customerEmail, Long workerId, String workerName, Integer itemsCount,
                BigDecimal subtotal, BigDecimal discount, BigDecimal tax, BigDecimal totalAmount,
                BigDecimal totalProfit, String paymentMethod, String paymentStatus,
                LocalDateTime saleDate, String createdBy) {
        this.invoiceNumber = invoiceNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.workerId = workerId;
        this.workerName = workerName;
        this.itemsCount = itemsCount;
        this.subtotal = subtotal;
        this.discount = discount;
        this.tax = tax;
        this.totalAmount = totalAmount;
        this.totalProfit = totalProfit;
        this.paymentMethod = paymentMethod != null ? paymentMethod : "CASH";
        this.paymentStatus = paymentStatus != null ? paymentStatus : "PAID";
        this.saleDate = saleDate != null ? saleDate : LocalDateTime.now();
        this.createdBy = createdBy;
    }

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.saleDate == null) this.saleDate = LocalDateTime.now();
        if (this.subtotal == null) this.subtotal = BigDecimal.ZERO;
        if (this.discount == null) this.discount = BigDecimal.ZERO;
        if (this.tax == null) this.tax = BigDecimal.ZERO;
        if (this.totalAmount == null) this.totalAmount = BigDecimal.ZERO;
        if (this.totalProfit == null) this.totalProfit = BigDecimal.ZERO;
    }

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    // Getters and Setters
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

    public List<SaleItem> getItems() { return items; }
    public void setItems(List<SaleItem> items) { this.items = items; }
}
