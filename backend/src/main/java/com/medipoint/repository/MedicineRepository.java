package com.medipoint.repository;

import com.medipoint.model.Medicine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    boolean existsByNameIgnoreCaseAndBatchNumberIgnoreCase(String name, String batchNumber);

    boolean existsByNameIgnoreCaseAndBatchNumberIgnoreCaseAndIdNot(String name, String batchNumber, Long id);

    Page<Medicine> findByStatus(String status, Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE " +
           "(:status IS NULL OR m.status = :status) AND (" +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Medicine> searchMedicines(@Param("query") String query,
                                   @Param("status") String status,
                                   Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND m.currentStock > 0 AND m.expiryDate >= :today AND (" +
           ":query IS NULL OR :query = '' OR " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.genericName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.brand) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(m.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
    List<Medicine> searchForPos(@Param("query") String query,
                                @Param("today") LocalDate today,
                                Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND m.currentStock > 0 AND m.currentStock <= m.minimumStock")
    Page<Medicine> findLowStock(Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND m.currentStock = 0")
    Page<Medicine> findOutOfStock(Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND m.expiryDate >= :today AND m.expiryDate <= :threshold")
    Page<Medicine> findExpiringSoon(@Param("today") LocalDate today,
                                    @Param("threshold") LocalDate threshold,
                                    Pageable pageable);

    @Query("SELECT m FROM Medicine m WHERE m.status = 'ACTIVE' AND m.expiryDate < :today")
    Page<Medicine> findExpired(@Param("today") LocalDate today, Pageable pageable);

    long countByStatus(String status);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.status = 'ACTIVE' AND m.currentStock > 0 AND m.currentStock <= m.minimumStock")
    long countLowStock();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.status = 'ACTIVE' AND m.currentStock = 0")
    long countOutOfStock();

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.status = 'ACTIVE' AND m.expiryDate >= :today AND m.expiryDate <= :threshold")
    long countExpiringSoon(@Param("today") LocalDate today, @Param("threshold") LocalDate threshold);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.status = 'ACTIVE' AND m.expiryDate < :today")
    long countExpired(@Param("today") LocalDate today);

    @Query("SELECT COALESCE(SUM(m.currentStock), 0) FROM Medicine m WHERE m.status = 'ACTIVE'")
    long sumTotalStockUnits();
}
