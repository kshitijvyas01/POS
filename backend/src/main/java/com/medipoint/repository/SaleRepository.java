package com.medipoint.repository;

import com.medipoint.model.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    Optional<Sale> findByInvoiceNumber(String invoiceNumber);

    boolean existsByInvoiceNumber(String invoiceNumber);

    Page<Sale> findAllByOrderBySaleDateDesc(Pageable pageable);

    @Query("SELECT s FROM Sale s WHERE " +
           "(:query IS NULL OR :query = '' OR " +
           "LOWER(s.invoiceNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.customerPhone) LIKE LOWER(CONCAT('%', :query, '%'))) AND " +
           "(:workerId IS NULL OR s.workerId = :workerId) AND " +
           "(:startDate IS NULL OR s.saleDate >= :startDate) AND " +
           "(:endDate IS NULL OR s.saleDate <= :endDate) " +
           "ORDER BY s.saleDate DESC")
    Page<Sale> filterSales(@Param("query") String query,
                           @Param("workerId") Long workerId,
                           @Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate,
                           Pageable pageable);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate >= :startOfDay AND s.saleDate <= :endOfDay AND s.paymentStatus = 'PAID'")
    long countTodaySales(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.saleDate >= :startOfDay AND s.saleDate <= :endOfDay AND s.paymentStatus = 'PAID'")
    BigDecimal sumTodayRevenue(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT COALESCE(SUM(s.totalProfit), 0) FROM Sale s WHERE s.saleDate >= :startOfDay AND s.saleDate <= :endOfDay AND s.paymentStatus = 'PAID'")
    BigDecimal sumTodayProfit(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    List<Sale> findTop5ByOrderBySaleDateDesc();
}
