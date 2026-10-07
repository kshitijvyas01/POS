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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SaleItemRepository saleItemRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @InjectMocks
    private SaleService saleService;

    private UserPrincipal ownerUser;
    private UserPrincipal workerUser;
    private Medicine activeMedicine;

    @BeforeEach
    void setUp() {
        User ownerEntity = new User("Store Owner", "owner@medipoint.com", "+91 9876543210", "pass", UserRole.OWNER, UserStatus.ACTIVE);
        ownerEntity.setId(1L);
        ownerUser = UserPrincipal.create(ownerEntity);

        User workerEntity = new User("Pharmacy Worker", "worker@medipoint.com", "+91 9876543211", "pass", UserRole.WORKER, UserStatus.ACTIVE);
        workerEntity.setId(2L);
        workerUser = UserPrincipal.create(workerEntity);

        activeMedicine = new Medicine(
                "Paracetamol 500mg", "Acetaminophen", "Crocin", "Pain Relief", "GSK",
                "BATCH-PCM-01", LocalDate.now().minusMonths(2), LocalDate.now().plusMonths(12),
                new BigDecimal("10.00"), new BigDecimal("25.00"), 50, 10,
                "Strip", "SUP-1", "Pharma Dist", "Tablets", "ACTIVE"
        );
        activeMedicine.setId(10L);
    }

    @Test
    @DisplayName("Should successfully process checkout, deduct stock, log movement and calculate profit")
    void testCheckoutSuccess() {
        SaleItemRequestDto itemReq = new SaleItemRequestDto(10L, 5);
        SaleRequestDto request = new SaleRequestDto(
                "Ramesh Sharma", "+91 9876543210", "ramesh@example.com",
                BigDecimal.ZERO, BigDecimal.ZERO, "CASH", "PAID", List.of(itemReq)
        );

        when(saleRepository.countTodaySales(any(), any())).thenReturn(3L);
        when(saleRepository.existsByInvoiceNumber(any())).thenReturn(false);
        when(medicineRepository.findById(10L)).thenReturn(Optional.of(activeMedicine));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> {
            Sale s = invocation.getArgument(0);
            s.setId(501L);
            return s;
        });

        SaleResponseDto response = saleService.checkout(request, ownerUser);

        assertNotNull(response);
        assertTrue(response.getInvoiceNumber().startsWith("MED-"));
        assertEquals(new BigDecimal("125.00"), response.getTotalAmount()); // 5 * 25.00
        assertEquals(new BigDecimal("75.00"), response.getTotalProfit());  // 5 * (25.00 - 10.00) = 75.00
        assertEquals(45, activeMedicine.getCurrentStock());                // 50 - 5 = 45

        verify(medicineRepository, times(1)).save(activeMedicine);
        verify(stockMovementRepository, times(1)).save(any(StockMovement.class));
        verify(saleRepository, times(1)).save(any(Sale.class));
    }

    @Test
    @DisplayName("Worker checkout should complete successfully but mask profit in response")
    void testWorkerCheckoutMasksProfit() {
        SaleItemRequestDto itemReq = new SaleItemRequestDto(10L, 2);
        SaleRequestDto request = new SaleRequestDto(
                "Anita Desai", "+91 9876543222", null,
                BigDecimal.ZERO, BigDecimal.ZERO, "UPI", "PAID", List.of(itemReq)
        );

        when(medicineRepository.findById(10L)).thenReturn(Optional.of(activeMedicine));
        when(saleRepository.save(any(Sale.class))).thenAnswer(invocation -> invocation.getArgument(0));

        SaleResponseDto response = saleService.checkout(request, workerUser);

        assertNotNull(response);
        assertEquals(new BigDecimal("50.00"), response.getTotalAmount());
        assertNull(response.getTotalProfit(), "Worker must NOT see profit in sale response");
    }

    @Test
    @DisplayName("Should reject sale if medicine is expired")
    void testRejectExpiredMedicine() {
        Medicine expiredMed = new Medicine(
                "Old Antibiotic", "Amoxicillin", "Mox", "Antibiotic", "Cipla",
                "EXP-01", LocalDate.now().minusMonths(12), LocalDate.now().minusDays(2),
                new BigDecimal("20.00"), new BigDecimal("40.00"), 20, 5,
                "Strip", "SUP-1", "Dist", "Expired", "ACTIVE"
        );
        expiredMed.setId(20L);

        SaleItemRequestDto itemReq = new SaleItemRequestDto(20L, 1);
        SaleRequestDto request = new SaleRequestDto(
                "Walk-in", null, null, BigDecimal.ZERO, BigDecimal.ZERO, "CASH", "PAID", List.of(itemReq)
        );

        when(medicineRepository.findById(20L)).thenReturn(Optional.of(expiredMed));

        CustomException ex = assertThrows(CustomException.class, () -> saleService.checkout(request, workerUser));
        assertTrue(ex.getMessage().contains("expired"));
        verify(saleRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject sale if requested quantity exceeds current stock")
    void testRejectInsufficientStock() {
        SaleItemRequestDto itemReq = new SaleItemRequestDto(10L, 999);
        SaleRequestDto request = new SaleRequestDto(
                "Walk-in", null, null, BigDecimal.ZERO, BigDecimal.ZERO, "CASH", "PAID", List.of(itemReq)
        );

        when(medicineRepository.findById(10L)).thenReturn(Optional.of(activeMedicine));

        CustomException ex = assertThrows(CustomException.class, () -> saleService.checkout(request, workerUser));
        assertTrue(ex.getMessage().contains("Insufficient stock"));
        verify(saleRepository, never()).save(any());
    }
}
