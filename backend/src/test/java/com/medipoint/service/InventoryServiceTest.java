package com.medipoint.service;

import com.medipoint.dto.InventorySummaryDto;
import com.medipoint.dto.StockAdjustmentDto;
import com.medipoint.dto.StockMovementResponseDto;
import com.medipoint.exception.CustomException;
import com.medipoint.model.Medicine;
import com.medipoint.model.StockMovement;
import com.medipoint.model.StockMovementType;
import com.medipoint.repository.MedicineRepository;
import com.medipoint.repository.StockMovementRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private InventoryService inventoryService;

    private Medicine testMedicine;

    @BeforeEach
    void setUp() {
        testMedicine = new Medicine(
                "Cetirizine 10mg", "Cetirizine", "Cetzine", "Allergy", "Dr. Reddy's",
                "CET-101", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(18),
                new BigDecimal("10.00"), new BigDecimal("20.00"), 40, 15,
                "Strip", "SUP-1", "Pharma Dist", "Allergy tabs", "ACTIVE"
        );
        testMedicine.setId(202L);
    }

    @Test
    @DisplayName("Should adjust stock and record stock movement")
    void testAdjustStockSuccess() {
        StockAdjustmentDto req = new StockAdjustmentDto(202L, 65, "Physical stock count verification");

        when(medicineRepository.findById(202L)).thenReturn(Optional.of(testMedicine));
        when(stockMovementRepository.save(any(StockMovement.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StockMovementResponseDto result = inventoryService.adjustStock(req, "owner_user");

        assertNotNull(result);
        assertEquals(40, result.getPreviousStock());
        assertEquals(65, result.getNewStock());
        assertEquals(25, result.getQuantity());
        assertEquals(StockMovementType.ADJUSTMENT, result.getType());
        assertEquals(65, testMedicine.getCurrentStock());

        verify(medicineRepository, times(1)).save(testMedicine);
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
    }

    @Test
    @DisplayName("Should reject negative stock adjustment")
    void testNegativeStockAdjustmentRejected() {
        StockAdjustmentDto req = new StockAdjustmentDto(202L, -5, "Damaged stock");
        when(medicineRepository.findById(202L)).thenReturn(Optional.of(testMedicine));

        assertThrows(CustomException.class, () -> inventoryService.adjustStock(req, "owner_user"));
        verify(stockMovementRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should calculate inventory summary correctly")
    void testInventorySummary() {
        when(medicineRepository.countByStatus("ACTIVE")).thenReturn(15L);
        when(medicineRepository.sumTotalStockUnits()).thenReturn(450L);
        when(medicineRepository.countLowStock()).thenReturn(2L);
        when(medicineRepository.countOutOfStock()).thenReturn(1L);
        when(medicineRepository.countExpiringSoon(any(), any())).thenReturn(3L);
        when(medicineRepository.countExpired(any())).thenReturn(0L);

        InventorySummaryDto summary = inventoryService.getSummary(false);

        assertEquals(15L, summary.getTotalMedicines());
        assertEquals(450L, summary.getTotalStockUnits());
        assertEquals(2L, summary.getLowStockCount());
        assertEquals(1L, summary.getOutOfStockCount());
        assertEquals(3L, summary.getExpiringSoonCount());
        assertEquals(0L, summary.getExpiredCount());
        assertNull(summary.getTotalStockValue(), "Worker should not see inventory monetary valuation");
    }
}
