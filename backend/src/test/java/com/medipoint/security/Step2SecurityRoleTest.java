package com.medipoint.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.medipoint.controller.InventoryController;
import com.medipoint.controller.MedicineController;
import com.medipoint.dto.MedicineDto;
import com.medipoint.dto.MedicineRequestDto;
import com.medipoint.dto.StockAdjustmentDto;
import com.medipoint.dto.StockMovementResponseDto;
import com.medipoint.model.StockMovementType;
import com.medipoint.repository.UserRepository;
import com.medipoint.service.InventoryService;
import com.medipoint.service.MedicineService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {MedicineController.class, InventoryController.class})
@Import(Step2SecurityRoleTest.TestSecurityConfig.class)
class Step2SecurityRoleTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MedicineService medicineService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private ObjectMapper objectMapper;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        public MedicineService medicineService() {
            return mock(MedicineService.class);
        }

        @Bean
        public InventoryService inventoryService() {
            return mock(InventoryService.class);
        }

        @Bean
        public JwtService jwtService() {
            return new JwtService();
        }

        @Bean
        public CustomUserDetailsService customUserDetailsService() {
            return new CustomUserDetailsService(mock(UserRepository.class));
        }

        @Bean
        public ObjectMapper objectMapper() {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule());
            return mapper;
        }

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(ex -> ex
                    .authenticationEntryPoint((req, res, e) -> {
                        res.setStatus(401);
                        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        res.getWriter().write("{\"success\":false,\"errorCode\":\"UNAUTHORIZED\",\"message\":\"Authentication required\"}");
                    })
                    .accessDeniedHandler((req, res, e) -> {
                        res.setStatus(403);
                        res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        res.getWriter().write("{\"success\":false,\"errorCode\":\"FORBIDDEN\",\"message\":\"Access denied\"}");
                    })
                )
                .authorizeHttpRequests(auth -> auth
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Test
    @DisplayName("Unauthenticated request to medicines endpoint should return 401 Unauthorized")
    void testUnauthenticatedMedicinesAccess() throws Exception {
        mockMvc.perform(get("/api/v1/medicines")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "worker@medipoint.com", roles = {"WORKER"})
    @DisplayName("Worker role attempting to create medicine should be rejected with 403 Forbidden")
    void testWorkerCreateMedicineForbidden() throws Exception {
        MedicineRequestDto req = new MedicineRequestDto();
        req.setName("Paracetamol 500mg");
        req.setBatchNumber("BATCH-1");
        req.setExpiryDate(LocalDate.now().plusMonths(6));
        req.setPurchasePrice(BigDecimal.TEN);
        req.setSellingPrice(BigDecimal.valueOf(20));

        mockMvc.perform(post("/api/v1/medicines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_ACCESS"));
    }

    @Test
    @WithMockUser(username = "worker@medipoint.com", roles = {"WORKER"})
    @DisplayName("Worker role attempting stock adjustment should be rejected with 403 Forbidden")
    void testWorkerStockAdjustmentForbidden() throws Exception {
        StockAdjustmentDto req = new StockAdjustmentDto(1L, 100, "Correction");

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN_ACCESS"));
    }

    @Test
    @WithMockUser(username = "worker@medipoint.com", roles = {"WORKER"})
    @DisplayName("Worker role can search medicines for POS successfully")
    void testWorkerCanSearchPosMedicines() throws Exception {
        when(medicineService.searchForPos("Crocin")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/medicines/pos-search?query=Crocin")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "owner@medipoint.com", roles = {"OWNER"})
    @DisplayName("Owner role can create medicine successfully with 201 Created")
    void testOwnerCreateMedicineAllowed() throws Exception {
        MedicineRequestDto req = new MedicineRequestDto();
        req.setName("Paracetamol 500mg");
        req.setBatchNumber("BATCH-1");
        req.setExpiryDate(LocalDate.now().plusMonths(6));
        req.setPurchasePrice(BigDecimal.TEN);
        req.setSellingPrice(BigDecimal.valueOf(20));

        MedicineDto dto = new MedicineDto();
        dto.setId(1L);
        dto.setName("Paracetamol 500mg");
        when(medicineService.createMedicine(any(), any())).thenReturn(dto);

        mockMvc.perform(post("/api/v1/medicines")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Paracetamol 500mg"));
    }

    @Test
    @WithMockUser(username = "owner@medipoint.com", roles = {"OWNER"})
    @DisplayName("Owner role can adjust stock successfully")
    void testOwnerAdjustStockAllowed() throws Exception {
        StockAdjustmentDto req = new StockAdjustmentDto(1L, 50, "Physical stock count");
        StockMovementResponseDto mov = new StockMovementResponseDto();
        mov.setId(10L);
        mov.setType(StockMovementType.ADJUSTMENT);
        mov.setNewStock(50);

        when(inventoryService.adjustStock(any(), any())).thenReturn(mov);

        mockMvc.perform(post("/api/v1/inventory/adjust")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
