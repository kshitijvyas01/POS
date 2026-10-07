package com.medipoint.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.medipoint.controller.SettingsController;
import com.medipoint.repository.UserRepository;
import com.medipoint.service.SettingsService;
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

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = SettingsController.class)
@Import(SecurityRoleAuthorizationTest.TestSecurityConfig.class)
class SecurityRoleAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SettingsService settingsService;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class TestSecurityConfig {

        @Bean
        public SettingsService settingsService() {
            return new SettingsService();
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
            return new ObjectMapper();
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
                    .requestMatchers("/api/v1/settings/**").hasRole("OWNER")
                    .anyRequest().authenticated()
                );
            return http.build();
        }
    }

    @Test
    @DisplayName("Unauthenticated request to Owner Settings should return 401 Unauthorized")
    void testUnauthenticatedAccessDenied() throws Exception {
        mockMvc.perform(get("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));
    }

    @Test
    @WithMockUser(username = "worker@medipoint.com", roles = {"WORKER"})
    @DisplayName("Worker role access to Owner Settings should be rejected with 403 Forbidden")
    void testWorkerRoleAccessForbidden() throws Exception {
        mockMvc.perform(get("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("FORBIDDEN"));
    }

    @Test
    @WithMockUser(username = "owner@medipoint.com", roles = {"OWNER"})
    @DisplayName("Owner role access to Owner Settings should succeed with 200 OK")
    void testOwnerRoleAccessAllowed() throws Exception {
        mockMvc.perform(get("/api/v1/settings")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.storeName").value("MediPoint Pharmacy"));
    }
}
