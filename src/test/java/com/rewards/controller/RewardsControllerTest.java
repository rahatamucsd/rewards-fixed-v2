package com.rewards.controller;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.dto.MonthlyReward;
import com.rewards.exception.CustomerNotFoundException;
import com.rewards.exception.GlobalExceptionHandler;
import com.rewards.service.RewardsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RewardsController.class)
@Import(GlobalExceptionHandler.class)
class RewardsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RewardsService rewardsService;

    private CustomerRewardSummary buildSummary(String id, String name) {
        MonthlyReward jan = new MonthlyReward(1, 2024, new BigDecimal("395.00"), 365);
        MonthlyReward feb = new MonthlyReward(2, 2024, new BigDecimal("263.00"), 148);
        MonthlyReward mar = new MonthlyReward(3, 2024, new BigDecimal("235.00"), 210);
        return new CustomerRewardSummary(id, name, Arrays.asList(jan, feb, mar), 723);
    }

    // ── GET /api/rewards?customerId=C001 — missing customerId → 400 ──────────

    @Test
    void missingCustomerId_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isBadRequest());
    }

    // ── GET /api/rewards?customerId=C001 — single customer response ──────────

    @Test
    void getOneCustomer_returns200AsOneElementArray() throws Exception {
        CustomerRewardSummary summary = buildSummary("C001", "Alice Johnson");
        when(rewardsService.getCustomerRewards("C001", 3)).thenReturn(summary);

        mockMvc.perform(get("/api/rewards?customerId=C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value("C001"))
                .andExpect(jsonPath("$[0].customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].totalRewardPoints").value(723));
    }

    @Test
    void getOneCustomer_withMonths_passesCorrectly() throws Exception {
        CustomerRewardSummary summary = buildSummary("C001", "Alice Johnson");
        when(rewardsService.getCustomerRewards("C001", 2)).thenReturn(summary);

        mockMvc.perform(get("/api/rewards?customerId=C001&months=2"))
                .andExpect(status().isOk());

        verify(rewardsService).getCustomerRewards("C001", 2);
    }

    // ── Error: customer not found → 404 ──────────────────────────────────────

    @Test
    void unknownCustomer_returns404WithNewSchema() throws Exception {
        when(rewardsService.getCustomerRewards("UNKNOWN", 3))
                .thenThrow(new CustomerNotFoundException("UNKNOWN"));

        mockMvc.perform(get("/api/rewards?customerId=UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    // ── Error: invalid months → 400 ───────────────────────────────────────────

    @Test
    void invalidMonths_zero_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards?months=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    void invalidMonths_string_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards?months=abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    // ── /points endpoint is removed ───────────────────────────────────────────

    @Test
    void pointsEndpoint_shouldNotExist_returns404() throws Exception {
        mockMvc.perform(get("/api/rewards/points?amount=120"))
                .andExpect(status().isNotFound());
    }
}
