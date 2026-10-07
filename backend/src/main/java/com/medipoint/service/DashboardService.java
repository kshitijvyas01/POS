package com.medipoint.service;

import com.medipoint.dto.DashboardSummaryDto;
import com.medipoint.dto.SaleResponseDto;
import com.medipoint.model.Sale;
import com.medipoint.model.UserRole;
import com.medipoint.repository.MedicineRepository;
import com.medipoint.repository.SaleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final MedicineRepository medicineRepository;
    private final SaleRepository saleRepository;

    public DashboardService(MedicineRepository medicineRepository, SaleRepository saleRepository) {
        this.medicineRepository = medicineRepository;
        this.saleRepository = saleRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSummaryDto getSummaryForRole(UserRole role) {
        boolean isOwner = (role == UserRole.OWNER);
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(LocalTime.MAX);

        long totalMedicines = medicineRepository.countByStatus("ACTIVE");
        long lowStockCount = medicineRepository.countLowStock();
        long outOfStockCount = medicineRepository.countOutOfStock();
        long expiringSoonCount = medicineRepository.countExpiringSoon(today, today.plusDays(30));
        long todaySalesCount = saleRepository.countTodaySales(startOfDay, endOfDay);

        String todayRevenueStr;
        String todayProfitStr;

        if (isOwner) {
            BigDecimal revenue = saleRepository.sumTodayRevenue(startOfDay, endOfDay);
            BigDecimal profit = saleRepository.sumTodayProfit(startOfDay, endOfDay);
            todayRevenueStr = String.format("₹%,.2f", revenue);
            todayProfitStr = String.format("₹%,.2f", profit);
        } else {
            todayRevenueStr = "RESTRICTED";
            todayProfitStr = "RESTRICTED";
        }

        List<Sale> recentSalesEntities = saleRepository.findTop5ByOrderBySaleDateDesc();
        List<SaleResponseDto> recentSales = recentSalesEntities.stream()
                .map(s -> SaleResponseDto.fromEntity(s, isOwner))
                .collect(Collectors.toList());

        List<Map<String, String>> alerts = new ArrayList<>();
        if (outOfStockCount > 0) {
            alerts.add(Map.of(
                    "id", "alert-out-of-stock",
                    "type", "DANGER",
                    "title", "Stock Depleted",
                    "message", outOfStockCount + " medicines are currently OUT OF STOCK. Reordering required.",
                    "time", "Immediate Action"
            ));
        }
        if (lowStockCount > 0) {
            alerts.add(Map.of(
                    "id", "alert-low-stock",
                    "type", "WARNING",
                    "title", "Low Stock Alert",
                    "message", lowStockCount + " medicines have fallen below their configured minimum threshold.",
                    "time", "Action Needed"
            ));
        }
        if (expiringSoonCount > 0) {
            alerts.add(Map.of(
                    "id", "alert-expiring",
                    "type", "WARNING",
                    "title", "Near Expiry Warning",
                    "message", expiringSoonCount + " batches are expiring within the next 30 days.",
                    "time", "Check Expiry"
            ));
        }
        if (alerts.isEmpty()) {
            alerts.add(Map.of(
                    "id", "alert-status-ok",
                    "type", "SUCCESS",
                    "title", "Store Health Optimal",
                    "message", "All active medicines are stocked above minimum thresholds with zero immediate expiries.",
                    "time", "Healthy"
            ));
        }

        String emptyNotice = recentSales.isEmpty()
                ? "No sales recorded yet today. Open Point of Sale (POS) to start billing."
                : null;

        return new DashboardSummaryDto(
                role,
                totalMedicines,
                lowStockCount,
                outOfStockCount,
                expiringSoonCount,
                todaySalesCount,
                todayRevenueStr,
                todayProfitStr,
                isOwner,
                recentSales,
                alerts,
                emptyNotice
        );
    }
}
