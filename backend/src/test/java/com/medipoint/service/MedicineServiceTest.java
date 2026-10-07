package com.medipoint.service;

import com.medipoint.dto.MedicineDto;
import com.medipoint.dto.MedicineRequestDto;
import com.medipoint.exception.CustomException;
import com.medipoint.model.Medicine;
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
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private MedicineService medicineService;

    private Medicine testMedicine;

    @BeforeEach
    void setUp() {
        testMedicine = new Medicine(
                "Paracetamol 500mg", "Acetaminophen", "Crocin", "Pain Relief", "GSK",
                "BATCH-001", LocalDate.now().minusMonths(1), LocalDate.now().plusMonths(12),
                new BigDecimal("15.00"), new BigDecimal("25.00"), 50, 10,
                "Strip", "SUP-1", "Pharma Dist", "Relief", "ACTIVE"
        );
        testMedicine.setId(101L);
    }

    @Test
    @DisplayName("Should create medicine and log initial stock movement")
    void testCreateMedicineSuccess() {
        MedicineRequestDto req = new MedicineRequestDto();
        req.setName("Paracetamol 500mg");
        req.setBatchNumber("BATCH-001");
        req.setPurchasePrice(new BigDecimal("15.00"));
        req.setSellingPrice(new BigDecimal("25.00"));
        req.setCurrentStock(50);
        req.setExpiryDate(LocalDate.now().plusMonths(12));

        when(medicineRepository.existsByNameIgnoreCaseAndBatchNumberIgnoreCase("Paracetamol 500mg", "BATCH-001"))
                .thenReturn(false);
        when(medicineRepository.save(any(Medicine.class))).thenAnswer(invocation -> {
            Medicine m = invocation.getArgument(0);
            m.setId(101L);
            return m;
        });

        MedicineDto result = medicineService.createMedicine(req, "owner_user");

        assertNotNull(result);
        assertEquals("Paracetamol 500mg", result.getName());
        assertEquals("BATCH-001", result.getBatchNumber());
        assertEquals(50, result.getCurrentStock());
        verify(stockMovementRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("Should reject duplicate medicine name and batch number")
    void testCreateMedicineDuplicateBatchRejected() {
        MedicineRequestDto req = new MedicineRequestDto();
        req.setName("Paracetamol 500mg");
        req.setBatchNumber("BATCH-001");

        when(medicineRepository.existsByNameIgnoreCaseAndBatchNumberIgnoreCase("Paracetamol 500mg", "BATCH-001"))
                .thenReturn(true);

        assertThrows(CustomException.class, () -> medicineService.createMedicine(req, "owner_user"));
        verify(medicineRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should mask purchase price when retrieved by worker")
    void testWorkerCannotSeePurchasePrice() {
        when(medicineRepository.findById(101L)).thenReturn(Optional.of(testMedicine));

        MedicineDto ownerView = medicineService.getMedicineById(101L, true);
        assertNotNull(ownerView.getPurchasePrice(), "Owner should see purchase price");
        assertEquals(new BigDecimal("15.00"), ownerView.getPurchasePrice());

        MedicineDto workerView = medicineService.getMedicineById(101L, false);
        assertNull(workerView.getPurchasePrice(), "Worker must NOT see purchase price");
        assertEquals(new BigDecimal("25.00"), workerView.getSellingPrice());
    }
}
