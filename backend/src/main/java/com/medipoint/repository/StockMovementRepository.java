package com.medipoint.repository;

import com.medipoint.model.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    Page<StockMovement> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<StockMovement> findByMedicineIdOrderByCreatedAtDesc(Long medicineId, Pageable pageable);
}
