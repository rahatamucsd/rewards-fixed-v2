package com.rewards.controller;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.service.RewardsService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rewards")
@Validated
public class RewardsController {

    private static final Logger log = LoggerFactory.getLogger(RewardsController.class);

    private final RewardsService rewardsService;

    @Autowired
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping
    public ResponseEntity<CustomerRewardSummary> getRewards(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "3") @Min(1) @Max(12) int months) {

        log.info("GET /api/rewards customerId={} months={}", customerId, months);
        CustomerRewardSummary result = rewardsService.getCustomerRewards(customerId, months);
        log.info("Returning {} monthly buckets, {} total points for customer {}",
                result.getMonthlyRewards().size(), result.getTotalRewardPoints(), customerId);
        return ResponseEntity.ok(result);
    }
}
