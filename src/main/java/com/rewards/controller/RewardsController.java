package com.rewards.controller;

import com.rewards.dto.CustomerRewardSummary;
import com.rewards.service.RewardsService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/rewards")
@Validated
public class RewardsController {

    private final RewardsService rewardsService;

    @Autowired
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping
    public ResponseEntity<List<CustomerRewardSummary>> getRewards(
            @RequestParam String customerId,
            @RequestParam(defaultValue = "3") @Min(1) int months) {

        return ResponseEntity.ok(Collections.singletonList(
                rewardsService.getCustomerRewards(customerId, months)));
    }
}
