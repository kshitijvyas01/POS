package com.medipoint.service;

import com.medipoint.dto.SaleItemRequestDto;
import com.medipoint.dto.SaleRequestDto;
import com.medipoint.dto.SaleResponseDto;
import com.medipoint.exception.CustomException;
import com.medipoint.model.*;
import com.medipoint.repository.MedicineRepository;
import com.medipoint.repository.SaleItemRepository;
import com.medipoint.repository.SaleRepository;
import com.medipoint.repository.StockMovementRepository;
import com.medipoint.security.UserPrincipal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class SaleService {

    private final SaleRepository saleRepository;
    private final SaleItemRepository saleItemRepository;
    private final MedicineRepository medicineRepository;
    private final StockMovementRepository stockMovementRepository;

    public SaleService(SaleRepository saleRepository,
                       SaleItemRepository saleItemRepository,
                       MedicineRepository medicineRepository,
                       StockMovementRepository stockMovementRepository) {
        this.saleRepository = saleRepository;
        this.saleItemRepository = saleItemRepository;
        this.medicineRepository = medicineRepository;
        this.stockMovementRepository = stockMovementRepository;
    }

    @Transactional
    public SaleResponseDto checkout(SaleRequestDto request, UserPrincipal currentUser) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw CustomException.badRequest("Cannot process checkout: Cart is empty.");
        }

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();
        boolean isOwner = currentUser.getRole() == UserRole.OWNER;

        // 1. Generate unique invoice number: MED-YYYYMMDD-XXXX
        String datePrefix = today.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);
        long todaySalesCount = saleRepository.countTodaySales(startOfDay, endOfDay);
        String invoiceNumber = String.format("MED-%s-%04d", datePrefix, todaySalesCount + 1);

        // Ensure uniqueness if collision occurs
        int retry = 1;
        while (saleRepository.existsByInvoiceNumber(invoiceNumber)) {
            invoiceNumber = String.format("MED-%s-%04d", datePrefix, todaySalesCount + 1 + retry);
            retry++;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        BigDecimal totalProfit = BigDecimal.ZERO;
        int totalItemsCount = 0;
        List<SaleItem> saleItems = new ArrayList<>();

        // 2. Validate items, check stock, check expiry, compute authoritative line totals & profit
        for (SaleItemRequestDto itemReq : request.getItems()) {
            if (itemReq.getMedicineId() == null) {
                throw CustomException.badRequest("Medicine ID cannot be null in cart.");
            }
            if (itemReq.getQuantity() == null || itemReq.getQuantity() <= 0) {
                throw CustomException.badRequest("Invalid quantity for medicine ID: " + itemReq.getMedicineId());
            }

            Medicine medicine = medicineRepository.findById(itemReq.getMedicineId())
                    .orElseThrow(() -> CustomException.notFound("Medicine not found with ID: " + itemReq.getMedicineId()));

            // Safety Rule 1: Active medicine check
            if (!"ACTIVE".equalsIgnoreCase(medicine.getStatus())) {
                throw CustomException.badRequest("Medicine '" + medicine.getName() + "' is inactive and cannot be sold.");
            }

            // Safety Rule 2: Expiry check (Expired medicines cannot be sold)
            if (medicine.isExpired()) {
                throw CustomException.badRequest("Medicine '" + medicine.getName() + "' (Batch " + medicine.getBatchNumber() + ") is expired on " + medicine.getExpiryDate() + " and cannot be sold.");
            }

            // Safety Rule 3 & 4: Stock check (Cannot exceed current stock)
            if (medicine.getCurrentStock() < itemReq.getQuantity()) {
                throw CustomException.badRequest("Insufficient stock for '" + medicine.getName() + "'. Available stock: " + medicine.getCurrentStock() + ", requested quantity: " + itemReq.getQuantity());
            }

            // Calculations
            BigDecimal sellingPrice = medicine.getSellingPrice();
            BigDecimal purchasePrice = medicine.getPurchasePrice();
            int qty = itemReq.getQuantity();

            BigDecimal itemSubtotal = sellingPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal itemProfit = sellingPrice.subtract(purchasePrice).multiply(BigDecimal.valueOf(qty));

            subtotal = subtotal.add(itemSubtotal);
            totalProfit = totalProfit.add(itemProfit);
            totalItemsCount += qty;

            // Reduce stock
            int prevStock = medicine.getCurrentStock();
            int newStock = prevStock - qty;
            medicine.setCurrentStock(newStock);
            medicine.setUpdatedBy(currentUser.getUsername());
            medicineRepository.save(medicine);

            // Record stock movement for this sale
            StockMovement movement = new StockMovement(
                    medicine,
                    medicine.getName(),
                    medicine.getBatchNumber(),
                    StockMovementType.SALE,
                    qty,
                    prevStock,
                    newStock,
                    invoiceNumber,
                    "Sale checkout: " + invoiceNumber,
                    currentUser.getUsername()
            );
            stockMovementRepository.save(movement);

            SaleItem saleItem = new SaleItem(
                    null, // set after Sale is created
                    medicine,
                    medicine.getName(),
                    medicine.getBatchNumber(),
                    qty,
                    sellingPrice,
                    purchasePrice,
                    itemSubtotal,
                    itemProfit
            );
            saleItems.add(saleItem);
        }

        // Apply discount and tax
        BigDecimal discount = request.getDiscount() != null ? request.getDiscount() : BigDecimal.ZERO;
        BigDecimal tax = request.getTax() != null ? request.getTax() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discount).add(tax);
        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // Net profit calculation: total line profit minus discount
        BigDecimal netProfit = totalProfit.subtract(discount);

        String paymentMethod = (request.getPaymentMethod() != null && !request.getPaymentMethod().isBlank())
                ? request.getPaymentMethod().toUpperCase()
                : "CASH";
        String paymentStatus = (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank())
                ? request.getPaymentStatus().toUpperCase()
                : "PAID";

        Sale sale = new Sale(
                invoiceNumber,
                null, // customerId can be linked in future CRM phase
                request.getCustomerName() != null ? request.getCustomerName().trim() : "Walk-in Customer",
                request.getCustomerPhone() != null ? request.getCustomerPhone().trim() : null,
                request.getCustomerEmail() != null ? request.getCustomerEmail().trim() : null,
                currentUser.getId(),
                currentUser.getFullName(),
                totalItemsCount,
                subtotal,
                discount,
                tax,
                totalAmount,
                netProfit,
                paymentMethod,
                paymentStatus,
                now,
                currentUser.getUsername()
        );

        for (SaleItem item : saleItems) {
            sale.addItem(item);
        }

        Sale savedSale = saleRepository.save(sale);
        return SaleResponseDto.fromEntity(savedSale, isOwner);
    }

    @Transactional(readOnly = true)
    public Page<SaleResponseDto> getSales(String query, Long workerId, LocalDateTime startDate,
                                          LocalDateTime endDate, int page, int size, boolean isOwner) {
        Pageable pageable = PageRequest.of(page, size);
        String cleanQuery = (query != null && !query.trim().isEmpty()) ? query.trim() : null;

        return saleRepository.filterSales(cleanQuery, workerId, startDate, endDate, pageable)
                .map(s -> SaleResponseDto.fromEntity(s, isOwner));
    }

    @Transactional(readOnly = true)
    public SaleResponseDto getSaleById(Long id, boolean isOwner) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> CustomException.notFound("Sale not found with ID: " + id));
        return SaleResponseDto.fromEntity(sale, isOwner);
    }

    @Transactional(readOnly = true)
    public SaleResponseDto getSaleByInvoiceNumber(String invoiceNumber, boolean isOwner) {
        Sale sale = saleRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> CustomException.notFound("Sale not found with Invoice: " + invoiceNumber));
        return SaleResponseDto.fromEntity(sale, isOwner);
    }
}
