package com.medipoint.dto;

import com.medipoint.model.UserRole;
import java.util.List;
import java.util.Map;

public class DashboardSummaryDto {
    private UserRole userRole;
    private long totalMedicines;
    private long lowStockCount;
    private long outOfStockCount;
    private long expiringSoonCount;
    private long todaySalesCount;
    private String todayRevenue;      // Masked "RESTRICTED" for Worker
    private String todayProfit;       // Masked "RESTRICTED" for Worker
    private boolean financialsVisible;
    private List<SaleResponseDto> recentSales;
    private List<Map<String, String>> recentAlerts;
    private String emptyStateNotice;

    public DashboardSummaryDto() {}

    public DashboardSummaryDto(UserRole userRole, long totalMedicines, long lowStockCount,
                               long outOfStockCount, long expiringSoonCount, long todaySalesCount,
                               String todayRevenue, String todayProfit, boolean financialsVisible,
                               List<SaleResponseDto> recentSales, List<Map<String, String>> recentAlerts,
                               String emptyStateNotice) {
        this.userRole = userRole;
        this.totalMedicines = totalMedicines;
        this.lowStockCount = lowStockCount;
        this.outOfStockCount = outOfStockCount;
        this.expiringSoonCount = expiringSoonCount;
        this.todaySalesCount = todaySalesCount;
        this.todayRevenue = todayRevenue;
        this.todayProfit = todayProfit;
        this.financialsVisible = financialsVisible;
        this.recentSales = recentSales;
        this.recentAlerts = recentAlerts;
        this.emptyStateNotice = emptyStateNotice;
    }

    public UserRole getUserRole() { return userRole; }
    public void setUserRole(UserRole userRole) { this.userRole = userRole; }

    public long getTotalMedicines() { return totalMedicines; }
    public void setTotalMedicines(long totalMedicines) { this.totalMedicines = totalMedicines; }

    public long getLowStockCount() { return lowStockCount; }
    public void setLowStockCount(long lowStockCount) { this.lowStockCount = lowStockCount; }

    public long getOutOfStockCount() { return outOfStockCount; }
    public void setOutOfStockCount(long outOfStockCount) { this.outOfStockCount = outOfStockCount; }

    public long getExpiringSoonCount() { return expiringSoonCount; }
    public void setExpiringSoonCount(long expiringSoonCount) { this.expiringSoonCount = expiringSoonCount; }

    public long getTodaySalesCount() { return todaySalesCount; }
    public void setTodaySalesCount(long todaySalesCount) { this.todaySalesCount = todaySalesCount; }

    public String getTodayRevenue() { return todayRevenue; }
    public void setTodayRevenue(String todayRevenue) { this.todayRevenue = todayRevenue; }

    public String getTodayProfit() { return todayProfit; }
    public void setTodayProfit(String todayProfit) { this.todayProfit = todayProfit; }

    public boolean isFinancialsVisible() { return financialsVisible; }
    public void setFinancialsVisible(boolean financialsVisible) { this.financialsVisible = financialsVisible; }

    public List<SaleResponseDto> getRecentSales() { return recentSales; }
    public void setRecentSales(List<SaleResponseDto> recentSales) { this.recentSales = recentSales; }

    public List<Map<String, String>> getRecentAlerts() { return recentAlerts; }
    public void setRecentAlerts(List<Map<String, String>> recentAlerts) { this.recentAlerts = recentAlerts; }

    public String getEmptyStateNotice() { return emptyStateNotice; }
    public void setEmptyStateNotice(String emptyStateNotice) { this.emptyStateNotice = emptyStateNotice; }
}
