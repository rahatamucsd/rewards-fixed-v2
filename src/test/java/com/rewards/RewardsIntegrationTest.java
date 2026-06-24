package com.rewards;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RewardsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void missingCustomerId_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getSingleCustomer_alice_returnsOneElementArray() throws Exception {
        mockMvc.perform(get("/api/rewards?customerId=C001"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].customerId").value("C001"))
                .andExpect(jsonPath("$[0].customerName").value("Alice Johnson"))
                .andExpect(jsonPath("$[0].totalRewardPoints").value(723))
                .andExpect(jsonPath("$[0].monthlyRewards.length()").value(3));
    }

    @Test
    void getSingleCustomer_months2_returnsOnlyTwoMonths() throws Exception {
        mockMvc.perform(get("/api/rewards?customerId=C001&months=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].monthlyRewards.length()").value(2));
    }

    @Test
    void unknownCustomer_returns404() throws Exception {
        mockMvc.perform(get("/api/rewards?customerId=UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404))
                .andExpect(jsonPath("$.path").value("/api/rewards"));
    }

    @Test
    void invalidMonths_returns400() throws Exception {
        mockMvc.perform(get("/api/rewards?months=0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }


}
